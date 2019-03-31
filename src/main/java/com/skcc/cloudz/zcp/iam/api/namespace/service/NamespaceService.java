package com.skcc.cloudz.zcp.iam.api.namespace.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.skcc.cloudz.zcp.iam.api.addon.service.AddonService;
import com.skcc.cloudz.zcp.iam.api.namespace.vo.ItemList;
import com.skcc.cloudz.zcp.iam.api.namespace.vo.NamespaceResourceDetailVO;
import com.skcc.cloudz.zcp.iam.api.namespace.vo.NamespaceResourceVO;
import com.skcc.cloudz.zcp.iam.api.namespace.vo.RoleBindingVO;
import com.skcc.cloudz.zcp.iam.common.exception.KeyCloakException;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpErrorCode;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpException;
import com.skcc.cloudz.zcp.iam.common.model.CPUUnit;
import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;
import com.skcc.cloudz.zcp.iam.common.model.MemoryUnit;
import com.skcc.cloudz.zcp.iam.common.model.ZcpLimitRange;
import com.skcc.cloudz.zcp.iam.common.model.ZcpResourceQuota;
import com.skcc.cloudz.zcp.iam.common.model.ZcpUser;
import com.skcc.cloudz.zcp.iam.common.model.ZcpUserList;
import com.skcc.cloudz.zcp.iam.common.util.Util;
import com.skcc.cloudz.zcp.iam.manager.KeyCloakManager;
import com.skcc.cloudz.zcp.iam.manager.KubeCoreManager;
import com.skcc.cloudz.zcp.iam.manager.KubeRbacAuthzManager;
import com.skcc.cloudz.zcp.iam.manager.ResourcesLabelManager;
import com.skcc.cloudz.zcp.iam.manager.ResourcesNameManager;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.V1ClusterRoleBinding;
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1LimitRange;
import io.kubernetes.client.models.V1LimitRangeItem;
import io.kubernetes.client.models.V1LimitRangeSpec;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1NamespaceList;
import io.kubernetes.client.models.V1NamespaceSpec;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1ResourceQuota;
import io.kubernetes.client.models.V1ResourceQuotaSpec;
import io.kubernetes.client.models.V1RoleBinding;
import io.kubernetes.client.models.V1RoleBindingList;
import io.kubernetes.client.models.V1RoleRef;
import io.kubernetes.client.models.V1Subject;

@Service
public class NamespaceService {

	private final Logger log = LoggerFactory.getLogger(NamespaceService.class);

	@Autowired
	private KeyCloakManager keyCloakManager;

	@Autowired
	private KubeCoreManager kubeCoreManager;

	@Autowired
	private KubeRbacAuthzManager kubeRbacAuthzManager;

	@Autowired
	private AddonService addonService;

	@Value("${zcp.kube.namespace}")
	private String zcpSystemNamespace;
	
	public V1NamespaceList getNamespaces() throws ZcpException {
		try {
			return kubeCoreManager.getNamespaceList();
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.NAMESAPCE_LIST_ERROR, e);
		}
	}

	public V1Namespace getNamespace(String namespace) throws ZcpException {
		try {
			return kubeCoreManager.getNamespace(namespace);
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.GET_NAMESPACE_ERROR, e);
		}
	}

	public NamespaceResourceDetailVO getNamespaceResource(String namespace, String userId) throws ZcpException {
		// check user privillege
		checkUserPrivilege(namespace, userId);

		// get namespace resource
		NamespaceResourceDetailVO namespaceDetail = new NamespaceResourceDetailVO();
		namespaceDetail.setNamespace(namespace);

		V1ResourceQuota v1ResourceQuota = null;
		try {
			v1ResourceQuota = kubeCoreManager.getResourceQuota(namespace,
					ResourcesNameManager.getResouceQuotaName(namespace));
		} catch (ApiException e) {
			// we can ignore this case
			log.debug("The resouece quota of " + namespace + " does not exist");
		}

		if (v1ResourceQuota != null) {
			Map<String, String> hard = v1ResourceQuota.getStatus().getHard();
			Map<String, String> used = v1ResourceQuota.getStatus().getUsed();
			if (hard != null && !hard.isEmpty()) {
				namespaceDetail.setHard(generateResourceQuota(hard));
				namespaceDetail.setUsed(generateResourceQuota(used));
			}
		}

		V1LimitRange v1LimitRange = null;
		try {
			v1LimitRange = kubeCoreManager.getLimitRange(namespace, ResourcesNameManager.getLimtRangeName(namespace));
		} catch (ApiException e) {
			// we can ignore this case
			log.debug("The limit range of " + namespace + " does not exist");
		}

		if (v1LimitRange != null) {
			namespaceDetail.setLimitRange(generateLimitRange(v1LimitRange));
		}

		return namespaceDetail;
	}

	private void checkUserPrivilege(String namespace, String userId) throws ZcpException {
		UserRepresentation userRepresentation = null;
		try {
			userRepresentation = keyCloakManager.getUser(userId);
		} catch (KeyCloakException e) {
			throw new ZcpException(ZcpErrorCode.USER_NOT_FOUND, "The user(" + userId + ") does not exist");
		}

		String username = userRepresentation.getUsername();
		log.debug("keyclock username is - {}", username);

		// check clusterrolebinding
		V1ClusterRoleBinding userClusterRoleBinding = null;
		try {
			userClusterRoleBinding = kubeRbacAuthzManager.getClusterRoleBindingByUsername(username);
		} catch (ApiException e2) {
			throw new ZcpException(ZcpErrorCode.CLUSTERROLEBINDING_NOT_FOUND, "The clusterrolebinding of user(" + userId + ") does not exist");
		}

		String userClusterRole = userClusterRoleBinding.getRoleRef().getName();
		boolean isClusterAdmin = StringUtils.equals(userClusterRole, ClusterRole.CLUSTER_ADMIN.getRole()) ? true
				: false;

		// check rolebinding of namespace
		boolean isNamespaceAdmin = false;
		if (!isClusterAdmin) {
			V1RoleBinding userNamespaceRoleBinding = null;
			try {
				userNamespaceRoleBinding = kubeRbacAuthzManager.getRoleBindingByUserName(namespace, username);
			} catch (ApiException e) {
				throw new ZcpException(ZcpErrorCode.ROLEBINDING_NOT_FOUND,
						"The namespace(" + namespace + ") rolebinding of user(" + userId + ") does not exist");
			}

			String userNamespaceRole = userNamespaceRoleBinding.getRoleRef().getName();
			isNamespaceAdmin = StringUtils.equals(userNamespaceRole, ClusterRole.ADMIN.getRole()) ? true : false;

			if (!isClusterAdmin && !isNamespaceAdmin) {
				throw new ZcpException(ZcpErrorCode.PERMISSION_DENY, 
						"The user(" + userId + ") does not have a permission for namespace(" + namespace + ")");
			}
		}
	}

	public void saveNamespace(NamespaceResourceVO vo) throws ZcpException {

		String namespaceName = vo.getNamespace();

		V1Namespace currentV1Namespace = null;
		try {
			currentV1Namespace = kubeCoreManager.getNamespace(namespaceName);
		} catch (ApiException e) {
			// this case is create case
			// we can ignore this case
			log.debug("This case is a creating namespace case");
		}

		if (currentV1Namespace == null) {
			V1Namespace v1Namespace = new V1Namespace();
			v1Namespace.setApiVersion("v1");
			v1Namespace.setKind("Namespace");
			v1Namespace.setSpec(new V1NamespaceSpec().addFinalizersItem("kubernetes"));
			V1ObjectMeta namespaceMetadata = new V1ObjectMeta();
			namespaceMetadata.setName(vo.getNamespace());
			v1Namespace.setMetadata(namespaceMetadata);
			v1Namespace.getMetadata().setLabels(ResourcesLabelManager.getSystemLabels());
			
			try {
				kubeCoreManager.createNamespace(namespaceName, v1Namespace);
			} catch (ApiException e) {
				throw new ZcpException(ZcpErrorCode.CREATE_NAMESAPCE_ERROR, e);
			}
		}
		
		saveNamespaceResoruceQuota(vo.getResourceQuota(), namespaceName);

		saveNamespaceLimitRange(vo.getLimitRange(), namespaceName);
		
		if (vo.getZdbAdmin() != null && vo.getZdbAdmin() == true) {
            String label = ResourcesLabelManager.SYSTEM_ZDB_LABEL_NAME + "=" + ResourcesLabelManager.SYSTEM_LABEL_VALUE;
            this.createNamespaceLabel(namespaceName, label);
        }
		
		addonService.onCreateNamespace(namespaceName);
	}

	public void deleteNamespace(String namespace, String userId) throws ZcpException {
		// check user privillege
		checkUserPrivilege(namespace, userId);

		try {
			kubeCoreManager.deleteNamespace(namespace);

			addonService.onDeleteNamespace(namespace);
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.DELETE_NAMESPACE_ERROR, e);
		}
	}

	public ZcpUserList getUsersByNamespace(String namespace) throws ZcpException {
		V1RoleBindingList rolebindingList = null;
		try {
			rolebindingList = kubeRbacAuthzManager.getRoleBindingListByNamespace(namespace);
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.ROLE_BINDING_LIST_BY_NAMESPACE_ERROR, e);
		}
		List<V1RoleBinding> rolebindings = rolebindingList.getItems();
		List<UserRepresentation> keycloakUsers = keyCloakManager.getUserList();
		List<ZcpUser> zcpUsers = new ArrayList<ZcpUser>();

		for (V1RoleBinding rolebinding : rolebindings) {
			String rolebindingName = rolebinding.getMetadata().getName();
			for (UserRepresentation keycloakUser : keycloakUsers) {
				if (rolebindingName.equals(ResourcesNameManager.getRoleBindingName(keycloakUser.getUsername()))) {
					ZcpUser user = new ZcpUser();
					user.setId(keycloakUser.getId());
					user.setUsername(keycloakUser.getUsername());
					user.setEmail(keycloakUser.getEmail());
					user.setLastName(keycloakUser.getLastName());
					user.setFirstName(keycloakUser.getFirstName());
					user.setCreatedDate(new Date(keycloakUser.getCreatedTimestamp()));
					user.setEnabled(keycloakUser.isEnabled());
					user.setNamespacedRole(ClusterRole.getClusterRole(rolebinding.getRoleRef().getName()));

					zcpUsers.add(user);
				}
			}
		}

		ZcpUserList userlist = new ZcpUserList();
		userlist.setItems(zcpUsers);

		return userlist;

	}

	public ItemList<String> getAllLabels() throws ZcpException {
		List<String> allLabels = new ArrayList<>();
		for (V1Namespace namespace : getNamespaces().getItems()) {
			List<String> labels = Util.MapToList(namespace.getMetadata().getLabels());
			allLabels.addAll(labels);
		}

		List<String> items = allLabels.stream().distinct().collect(Collectors.toList());
		ItemList<String> item = new ItemList<>();
		item.setItems(items);
		return item;
	}

	public ItemList<String> getLabelsByNamespace(String namespaceName) throws ZcpException {
		V1Namespace namespace = getNamespace(namespaceName);
		Map<String, String> labels = namespace.getMetadata().getLabels();
		if (labels != null && !labels.isEmpty()) {
			ItemList<String> labelList = new ItemList<>();
			labelList.setItems(Util.MapToList(namespace.getMetadata().getLabels()));
			return labelList;
		}

		return null;
	}

	public void createNamespaceLabel(String namespaceName, String newLabel) throws ZcpException {
		V1Namespace namespace = getNamespace(namespaceName);
		Map<String, String> labels = namespace.getMetadata().getLabels();
		namespace.getMetadata().setLabels(addLabel(labels, newLabel));

		try {
			kubeCoreManager.replaceNamespace(namespaceName, namespace);
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.REPLACE_NAMESPACE_ERROR, e);
		}

	}

	public void deleteNamespaceLabel(String namespaceName, String label) throws ZcpException {
		V1Namespace namespace = getNamespace(namespaceName);
		Map<String, String> labels = namespace.getMetadata().getLabels();
		namespace.getMetadata().setLabels(removeLabel(labels, label));

		try {
			kubeCoreManager.replaceNamespace(namespaceName, namespace);
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.REPLACE_NAMESPACE_ERROR, e);
		}

	}

	public void deleteClusterRoleBinding(String clusterRoleBindingName) throws IOException, ApiException {
		kubeRbacAuthzManager.deleteClusterRoleBinding(clusterRoleBindingName);
	}

	public void createRoleBinding(String namespace, RoleBindingVO vo) throws ZcpException {
		V1RoleBinding roleBinding = makeRoleBinding(namespace, vo);

		try {
			roleBinding = kubeRbacAuthzManager.createRoleBinding(namespace, roleBinding);
			
			// add keycloak realm-roles
			addonService.addNamespaceRoles(namespace, vo.getUsername(), vo.getClusterRole());
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.CREATE_ROLE_BINDING_ERROR, e);
		}
	}

	public void editRoleBinding(String namespace, RoleBindingVO vo) throws ZcpException {
		// 1.delete RoleBinding & RealmRoles
		try {
			deleteRoleBinding(namespace, vo);
		} catch (ZcpException e) {
			// if this is new authorization
			if(e.getCode() != ZcpErrorCode.DELETE_ROLE_BINDING_ERROR)
				throw e;
		}

		// 2.create RoleBinding and add RealmRoles
		createRoleBinding(namespace, vo);
	}

	public void deleteRoleBinding(String namespace, RoleBindingVO data) throws ZcpException {
		V1DeleteOptions deleteOptions = new V1DeleteOptions();
		deleteOptions.setGracePeriodSeconds(0L);
		try {
			String username = data.getUsername();

			V1RoleBinding oldRoleBinding = kubeRbacAuthzManager.getRoleBindingByUserName(namespace, username);
			String oldRoleName = oldRoleBinding.getRoleRef().getName();

			// delete RoleBinding
			kubeRbacAuthzManager.deleteRoleBinding(namespace, ResourcesNameManager.getRoleBindingName(data.getUsername()), deleteOptions);

			// delete Keycloak Realm Roles
			addonService.deleteNamspaceRoles(namespace, username, ClusterRole.getClusterRole(oldRoleName));
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.DELETE_ROLE_BINDING_ERROR, e);
		}

	}

	private ZcpResourceQuota generateResourceQuota(Map<String, String> data) {
		ZcpResourceQuota resourceQuota = new ZcpResourceQuota();

		String value = data.get("limits.cpu");
		if (StringUtils.isNotEmpty(value)) {
			resourceQuota.setCpuLimits(getResourceValue(value));
			resourceQuota.setCpuLimitsUnit(getCPUUnitFormat(value));
		}
		value = data.get("limits.memory");
		if (StringUtils.isNotEmpty(value)) {
			resourceQuota.setMemoryLimits(getResourceValue(value));
			resourceQuota.setMemoryLimitsUnit(getMemoryUnitFormat(value));
		}
		value = data.get("requests.cpu");
		if (StringUtils.isNotEmpty(value)) {
			resourceQuota.setCpuRequests(getResourceValue(value));
			resourceQuota.setCpuRequestsUnit(getCPUUnitFormat(value));
		}
		value = data.get("requests.memory");
		if (StringUtils.isNotEmpty(value)) {
			resourceQuota.setMemoryRequests(getResourceValue(value));
			resourceQuota.setMemoryRequestsUnit(getMemoryUnitFormat(value));
		}

		value = data.get("configmaps");
		if (StringUtils.isNotEmpty(value)) {
			resourceQuota.setConfigmaps(getResourceValue(value));
		}
		value = data.get("persistentvolumeclaims");
		if (StringUtils.isNotEmpty(value)) {
			resourceQuota.setPersistentvolumeclaims(getResourceValue(value));
		}
		value = data.get("pods");
		if (StringUtils.isNotEmpty(value)) {
			resourceQuota.setPods(getResourceValue(value));
		}
		value = data.get("resourcequotas");
		if (StringUtils.isNotEmpty(value)) {
			resourceQuota.setResourcequotas(getResourceValue(value));
		}
		value = data.get("secrets");
		if (StringUtils.isNotEmpty(value)) {
			resourceQuota.setSecrets(getResourceValue(value));
		}
		value = data.get("services");
		if (StringUtils.isNotEmpty(value)) {
			resourceQuota.setServices(getResourceValue(value));
		}
		value = data.get("services.loadbalancers");
		if (StringUtils.isNotEmpty(value)) {
			resourceQuota.setServicesLoadbalancers(getResourceValue(value));
		}
		value = data.get("services.nodeports");
		if (StringUtils.isNotEmpty(value)) {
			resourceQuota.setServicesNodeports(getResourceValue(value));
		}
		value = data.get("replicationcontrollers");
		if (StringUtils.isNotEmpty(value)) {
			resourceQuota.setReplicationcontrollers(getResourceValue(value));
		}

		return resourceQuota;
	}

	private Integer getResourceValue(String value) {
		if (value == null)
			return null;

		Quantity quantity = new Quantity(value);
		if (quantity.getFormat() == Quantity.Format.BINARY_SI) {
			if (StringUtils.contains(value, "Gi"))
				return Integer.valueOf(StringUtils.substringBefore(value, "Gi"));
			if (StringUtils.contains(value, "Mi"))
				return Integer.valueOf(StringUtils.substringBefore(value, "Mi"));
			return null;
		} else {
			if (StringUtils.contains(value, "m"))
				return Integer.valueOf(StringUtils.substringBefore(value, "m"));
			return Integer.valueOf(value);
		}
	}

	private CPUUnit getCPUUnitFormat(String value) {
		if (value == null)
			return null;

		Quantity quantity = new Quantity(value);
		if (quantity.getFormat() == Quantity.Format.BINARY_SI) {
			return null;
		} else {
			if (StringUtils.contains(value, "m"))
				return CPUUnit.MilliCore;
			return CPUUnit.Core;
		}
	}

	private MemoryUnit getMemoryUnitFormat(String value) {
		if (value == null)
			return null;

		Quantity quantity = new Quantity(value);
		if (quantity.getFormat() == Quantity.Format.BINARY_SI) {
			if (StringUtils.contains(value, "Gi"))
				return MemoryUnit.Gi;
			if (StringUtils.contains(value, "Mi"))
				return MemoryUnit.Mi;
			return MemoryUnit.Gi;
		} else {
			return null;
		}
	}

	private ZcpLimitRange generateLimitRange(V1LimitRange v1LimitRange) {
		if (v1LimitRange == null)
			return null;
		List<V1LimitRangeItem> limitRangeItems = v1LimitRange.getSpec().getLimits();
		V1LimitRangeItem item = null;

		ZcpLimitRange limitRange = new ZcpLimitRange();

		if (limitRangeItems != null) {
			for (V1LimitRangeItem v1LimitRangeItem : limitRangeItems) {
				if (v1LimitRangeItem.getType().equals("Container")) {
					item = v1LimitRangeItem;
					break;
				}
			}

			Map<String, Quantity> defaultLimits = item.getDefault();
			Map<String, Quantity> defaultRequests = item.getDefaultRequest();

			limitRange.setCpuLimits(getResourceValue(defaultLimits.get("cpu")));
			limitRange.setCpuLimitsUnit(getCPUUnitFormat(defaultLimits.get("cpu")));
			limitRange.setMemoryLimits(getResourceValue(defaultLimits.get("memory")));
			limitRange.setMemoryLimitsUnit(getMemoryUnitFormat(defaultLimits.get("memory")));
			limitRange.setCpuRequests(getResourceValue(defaultRequests.get("cpu")));
			limitRange.setCpuRequestsUnit(getCPUUnitFormat(defaultRequests.get("cpu")));
			limitRange.setMemoryRequests(getResourceValue(defaultRequests.get("memory")));
			limitRange.setMemoryRequestsUnit(getMemoryUnitFormat(defaultRequests.get("memory")));
		}
		return limitRange;
	}

	private Integer getResourceValue(Quantity quantity) {
		if (quantity == null)
			return null;

		double value = quantity.getNumber().doubleValue();
		if (quantity.getFormat() == Quantity.Format.BINARY_SI) {
			if (value == 0) {
				return Integer.valueOf(StringUtils.substringBefore(String.valueOf(value), "."));
			}

			double formattedValue = value / 1024 / 1024;
			if (formattedValue % 1024 != 0) {
				return Integer.valueOf(StringUtils.substringBefore(String.valueOf(formattedValue), "."));
			} else {
				formattedValue = formattedValue / 1024;
				return Integer.valueOf(StringUtils.substringBefore(String.valueOf(formattedValue), "."));
			}
		} else {
			if (value == 0) {
				return Integer.valueOf(StringUtils.substringBefore(String.valueOf(value), "."));
			}

			if (value % 1 != 0) {
				double formattedValue = value * 1000;
				return Integer.valueOf(StringUtils.substringBefore(String.valueOf(formattedValue), "."));
			} else {
				return Integer.valueOf(StringUtils.substringBefore(String.valueOf(value), "."));
			}
		}
	}

	private CPUUnit getCPUUnitFormat(Quantity quantity) {
		if (quantity == null)
			return null;

		double value = quantity.getNumber().doubleValue();

		if (quantity.getFormat() == Quantity.Format.BINARY_SI) {
			return null;
		} else {
			if (value == 0) {
				return CPUUnit.Core;
			}

			if (value % 1 != 0) {
				return CPUUnit.MilliCore;
			} else {
				return CPUUnit.Core;
			}
		}
	}

	private MemoryUnit getMemoryUnitFormat(Quantity quantity) {
		if (quantity == null)
			return null;

		double value = quantity.getNumber().doubleValue();

		if (quantity.getFormat() != Quantity.Format.BINARY_SI) {
			return null;
		} else {
			if (value == 0) {
				return MemoryUnit.Gi;
			}

			double formattedValue = value / 1024 / 1024;
			if (formattedValue % 1024 != 0) {
				return MemoryUnit.Mi;
			} else {
				return MemoryUnit.Gi;
			}
		}
	}

	private void saveNamespaceLimitRange(ZcpLimitRange limitRange, String namespaceName) throws ZcpException {
		V1LimitRange currentV1LimitRange = null;
		try {
			currentV1LimitRange = kubeCoreManager.getLimitRange(namespaceName,
					ResourcesNameManager.getLimtRangeName(namespaceName));
		} catch (ApiException e1) {
			// this case is create case
			// we can ignore this case
			log.debug("This case is a creating limit range of namespace case");
		}

		if (limitRange.isEmpty()) {
			if (currentV1LimitRange != null) {
				try {
					kubeCoreManager.deleteLimitRange(namespaceName,
							ResourcesNameManager.getLimtRangeName(namespaceName));
				} catch (ApiException e) {
					throw new ZcpException(ZcpErrorCode.DELETE_LIMIT_RANGE_ERROR, e);
				}
			}
		} else {
			if (currentV1LimitRange == null) {
				V1LimitRange newV1LimitRange = generateV1LimitRange(limitRange, namespaceName);
				try {
					kubeCoreManager.createLimitRange(namespaceName, newV1LimitRange);
				} catch (ApiException e) {
					throw new ZcpException(ZcpErrorCode.CREATE_LIMIT_RANGE_ERROR, e);
				}
			} else {
				currentV1LimitRange = generateV1LimitRange(limitRange, namespaceName);
				try {
					kubeCoreManager.editLimitRange(namespaceName, ResourcesNameManager.getLimtRangeName(namespaceName),
							currentV1LimitRange);
				} catch (ApiException e) {
					throw new ZcpException(ZcpErrorCode.EDIT_LIMIT_RANGE_ERROR, e);
				}
			}
		}

	}

	private void saveNamespaceResoruceQuota(ZcpResourceQuota resourceQuota, String namespaceName) throws ZcpException {
		V1ResourceQuota currentV1ResourceQuota = null;
		try {
			currentV1ResourceQuota = kubeCoreManager.getResourceQuota(namespaceName,
					ResourcesNameManager.getResouceQuotaName(namespaceName));
		} catch (ApiException e1) {
			// this case is create case
			// we can ignore this case
			log.debug("This case is a creating resource quota of namespace case");
		}

		if (resourceQuota.isEmpty()) {
			if (currentV1ResourceQuota != null) {
				try {
					kubeCoreManager.deleteResourceQuota(namespaceName,
							ResourcesNameManager.getResouceQuotaName(namespaceName));
				} catch (ApiException e) {
					throw new ZcpException(ZcpErrorCode.DELETE_RESOURCE_QUOTA_ERROR, e);
				}
			}
		} else {
			if (currentV1ResourceQuota == null) {
				V1ResourceQuota newV1ResourceQuota = gerneateV1ResourceQuota(resourceQuota, namespaceName);
				try {
					kubeCoreManager.createResourceQuota(namespaceName, newV1ResourceQuota);
				} catch (ApiException e) {
					throw new ZcpException(ZcpErrorCode.CREATE_RESOURCE_QUOTA_ERROR, e);
				}
			} else {
				currentV1ResourceQuota = gerneateV1ResourceQuota(resourceQuota, namespaceName);
				try {
					kubeCoreManager.editResourceQuota(namespaceName,
							ResourcesNameManager.getResouceQuotaName(namespaceName), currentV1ResourceQuota);
				} catch (ApiException e) {
					throw new ZcpException(ZcpErrorCode.EDIT_RESOURCE_QUOTA_ERROR, e);
				}
			}
		}
	}

	private V1LimitRange generateV1LimitRange(ZcpLimitRange limitRange, String namespaceName) {
		V1LimitRange v1LimitRange = new V1LimitRange();
		// set metadata
		V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
		v1ObjectMeta.setName(ResourcesNameManager.getLimtRangeName(namespaceName));
		v1ObjectMeta.setLabels(ResourcesLabelManager.getSystemLabels());
		v1LimitRange.setMetadata(v1ObjectMeta);

		// set item
		V1LimitRangeItem v1LimitRangeItem = new V1LimitRangeItem();
		v1LimitRangeItem.setType("Container");

		Map<String, Quantity> defaultLimits = new HashMap<>();
		if (limitRange.getCpuLimitsFormat() != null)
			defaultLimits.put("cpu", Quantity.fromString(limitRange.getCpuLimitsFormat()));
		if (limitRange.getMemoryLimitsFormat() != null)
			defaultLimits.put("memory", Quantity.fromString(limitRange.getMemoryLimitsFormat()));
		v1LimitRangeItem.setDefault(defaultLimits);

		Map<String, Quantity> defaultRequests = new HashMap<>();
		if (limitRange.getCpuRequestsFormat() != null)
			defaultRequests.put("cpu", Quantity.fromString(limitRange.getCpuRequestsFormat()));
		if (limitRange.getMemoryRequestsFormat() != null)
			defaultRequests.put("memory", Quantity.fromString(limitRange.getMemoryRequestsFormat()));
		v1LimitRangeItem.setDefaultRequest(defaultRequests);

		// set spec
		V1LimitRangeSpec v1LimitRangeSpec = new V1LimitRangeSpec();
		v1LimitRangeSpec.addLimitsItem(v1LimitRangeItem);

		v1LimitRange.setSpec(v1LimitRangeSpec);

		return v1LimitRange;
	}

	private V1ResourceQuota gerneateV1ResourceQuota(ZcpResourceQuota resourceQuota, String namespaceName) {
		V1ResourceQuota v1ResourceQuota = new V1ResourceQuota();

		// set metadata
		V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
		v1ObjectMeta.setName(ResourcesNameManager.getResouceQuotaName(namespaceName));
		v1ObjectMeta.setLabels(ResourcesLabelManager.getSystemLabels());
		v1ResourceQuota.setMetadata(v1ObjectMeta);

		// set hard
		Map<String, Quantity> hard = new HashMap<>();
		if (resourceQuota.getCpuLimitsFormat() != null)
			hard.put("limits.cpu", Quantity.fromString(resourceQuota.getCpuLimitsFormat()));
		if (resourceQuota.getMemoryLimitsFormat() != null)
			hard.put("limits.memory", Quantity.fromString(resourceQuota.getMemoryLimitsFormat()));
		if (resourceQuota.getCpuRequestsFormat() != null)
			hard.put("requests.cpu", Quantity.fromString(resourceQuota.getCpuRequestsFormat()));
		if (resourceQuota.getMemoryRequestsFormat() != null)
			hard.put("requests.memory", Quantity.fromString(resourceQuota.getMemoryRequestsFormat()));

		if (resourceQuota.getConfigmaps() != null)
			hard.put("configmaps", Quantity.fromString(String.valueOf(resourceQuota.getConfigmaps())));
		if (resourceQuota.getPersistentvolumeclaims() != null)
			hard.put("persistentvolumeclaims",
					Quantity.fromString(String.valueOf(resourceQuota.getPersistentvolumeclaims())));
		if (resourceQuota.getPods() != null)
			hard.put("pods", Quantity.fromString(String.valueOf(resourceQuota.getPods())));
		if (resourceQuota.getResourcequotas() != null)
			hard.put("resourcequotas", Quantity.fromString(String.valueOf(resourceQuota.getResourcequotas())));
		if (resourceQuota.getSecrets() != null)
			hard.put("secrets", Quantity.fromString(String.valueOf(resourceQuota.getSecrets())));
		if (resourceQuota.getServices() != null)
			hard.put("services", Quantity.fromString(String.valueOf(resourceQuota.getServices())));
		if (resourceQuota.getServicesLoadbalancers() != null)
			hard.put("services.loadbalancers",
					Quantity.fromString(String.valueOf(resourceQuota.getServicesLoadbalancers())));
		if (resourceQuota.getServicesNodeports() != null)
			hard.put("services.nodeports", Quantity.fromString(String.valueOf(resourceQuota.getServicesNodeports())));
		if (resourceQuota.getReplicationcontrollers() != null)
			hard.put("replicationcontrollers",
					Quantity.fromString(String.valueOf(resourceQuota.getReplicationcontrollers())));

		// set spec
		V1ResourceQuotaSpec v1ResourceQuotaSpec = new V1ResourceQuotaSpec();
		v1ResourceQuotaSpec.setHard(hard);

		v1ResourceQuota.setSpec(v1ResourceQuotaSpec);

		return v1ResourceQuota;
	}

	private V1RoleBinding makeRoleBinding(String namespace, RoleBindingVO vo) {
		String username = vo.getUsername();
		String serviceAccountName = ResourcesNameManager.getServiceAccountName(username);
		String roleBindingName = ResourcesNameManager.getRoleBindingName(username);
		Map<String, String> labels = ResourcesLabelManager.getSystemUsernameLabels(username);

		V1ObjectMeta metadata = new V1ObjectMeta();
		metadata.setName(roleBindingName);
		metadata.setLabels(labels);
		metadata.setNamespace(namespace);

		V1RoleRef roleRef = new V1RoleRef();
		roleRef.setApiGroup("rbac.authorization.k8s.io");
		roleRef.setKind("ClusterRole");
		roleRef.setName(vo.getClusterRole().getRole());

		V1Subject subject = new V1Subject();
		subject.setKind("ServiceAccount");
		subject.setName(serviceAccountName);
		subject.setNamespace(zcpSystemNamespace);

		List<V1Subject> subjects = new ArrayList<V1Subject>();
		subjects.add(subject);

		V1RoleBinding roleBinding = new V1RoleBinding();
		// roleBinding.setApiVersion("rbac.authorization.k8s.io/v1");
		roleBinding.setMetadata(metadata);
		roleBinding.setRoleRef(roleRef);
		roleBinding.setSubjects(subjects);

		return roleBinding;
	}

	private Map<String, String> removeLabel(Map<String, String> labels, String label) {
		if (labels == null || labels.isEmpty()) {
			return labels;
		}

		if (StringUtils.isEmpty(label)) {
			return labels;
		}

		String[] map = label.split("=");
		if (map == null || map.length != 2) {
			log.debug("label is invalid - {}", label);
			return labels;
		}

		String key = map[0];
		String value = map[1];

		if (StringUtils.equals(value, labels.get(key))) {
			labels.remove(key);
		}

		return labels;
	}

	private Map<String, String> addLabel(Map<String, String> labels, String newLabel) throws ZcpException {
		if (StringUtils.isEmpty(newLabel)) {
			return labels;
		}

		String[] map = newLabel.split("=");
		if (map == null || map.length != 2) {
			log.debug("label is invalid - {}", newLabel);
			throw new ZcpException(ZcpErrorCode.LABEL_INVALID_ERROR,
					"label[" + newLabel + "] value is invalid. The label format should be 'key=value'");
		}

		String key = map[0];
		String value = map[1];

		if (labels == null) {
			labels = new HashMap<>();
		}

		labels.put(key, value);

		return labels;
	}
	
	public Object verify(String namespace, boolean dry) {
		Map<String, Object> ctx = Maps.newHashMap();
		ctx.put(NamespaceEventListener.DRY_RUN, dry);

		try {
			if("all".equals(namespace)) {
				List<V1Namespace> ns = getNamespaces().getItems();
				for(V1Namespace n : ns) {
					addonService.verify(n.getMetadata().getName(), ctx);
				}
			} else {
				addonService.verify(namespace, ctx);
			}
		} catch (Throwable e) {
			ctx.put(NamespaceEventListener.ERROR, e.getMessage());
		}
		
		return ctx;
	}
}
