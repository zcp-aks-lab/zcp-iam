package com.skcc.cloudz.zcp.iam.api.user.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.skcc.cloudz.zcp.iam.api.addon.service.KeycloakService;
import com.skcc.cloudz.zcp.iam.api.user.vo.MemberVO;
import com.skcc.cloudz.zcp.iam.api.user.vo.ResetCredentialVO;
import com.skcc.cloudz.zcp.iam.api.user.vo.ResetPasswordVO;
import com.skcc.cloudz.zcp.iam.api.user.vo.UpdateClusterRoleVO;
import com.skcc.cloudz.zcp.iam.api.user.vo.UpdatePasswordVO;
import com.skcc.cloudz.zcp.iam.api.user.vo.UserAttributeVO;
import com.skcc.cloudz.zcp.iam.common.exception.KeyCloakException;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpErrorCode;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpException;
import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;
import com.skcc.cloudz.zcp.iam.common.model.CredentialActionType;
import com.skcc.cloudz.zcp.iam.common.model.ZcpKubeConfig;
import com.skcc.cloudz.zcp.iam.common.model.ZcpKubeConfig.ClusterInfo;
import com.skcc.cloudz.zcp.iam.common.model.ZcpKubeConfig.ClusterInfo.Cluster;
import com.skcc.cloudz.zcp.iam.common.model.ZcpKubeConfig.ContextInfo;
import com.skcc.cloudz.zcp.iam.common.model.ZcpKubeConfig.ContextInfo.Context;
import com.skcc.cloudz.zcp.iam.common.model.ZcpKubeConfig.UserInfo;
import com.skcc.cloudz.zcp.iam.common.model.ZcpKubeConfig.UserInfo.User;
import com.skcc.cloudz.zcp.iam.common.model.ZcpUser;
import com.skcc.cloudz.zcp.iam.common.model.ZcpUserList;
import com.skcc.cloudz.zcp.iam.manager.KeyCloakManager;
import com.skcc.cloudz.zcp.iam.manager.KubeCoreManager;
import com.skcc.cloudz.zcp.iam.manager.KubeRbacAuthzManager;
import com.skcc.cloudz.zcp.iam.manager.KubeResourceManager;
import com.skcc.cloudz.zcp.iam.manager.ResourcesLabelManager;
import com.skcc.cloudz.zcp.iam.manager.ResourcesNameManager;
import com.skcc.cloudz.zcp.iam.manager.client.ServiceAccountApiKeyHolder;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1ClusterRoleBinding;
import io.kubernetes.client.models.V1ClusterRoleBindingList;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1NamespaceList;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1ObjectReference;
import io.kubernetes.client.models.V1RoleBinding;
import io.kubernetes.client.models.V1RoleBindingList;
import io.kubernetes.client.models.V1RoleRef;
import io.kubernetes.client.models.V1Secret;
import io.kubernetes.client.models.V1ServiceAccount;
import io.kubernetes.client.models.V1ServiceAccountList;
import io.kubernetes.client.models.V1Status;
import io.kubernetes.client.models.V1Subject;

@Service
public class UserService {

	private final Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private KeyCloakManager keyCloakManager;

	@Autowired
	private KubeCoreManager kubeCoreManager;

	@Autowired
	private KubeRbacAuthzManager kubeRbacAuthzManager;

	@Autowired
	private KubeResourceManager kubeResourceManager;

	@Autowired
	private KeycloakService keycloakService;

	@Value("${zcp.kube.namespace}")
	private String zcpSystemNamespace;

	@Value("${kube.server.apiserver.endpoint}")
	private String kubeApiServerEndpoint;

	public ZcpUserList getUsers(String keyword) throws ZcpException {
		List<UserRepresentation> keyCloakUsers = keyCloakManager.getUserList(keyword);

		List<ZcpUser> users = new ArrayList<ZcpUser>();
		for (UserRepresentation cloakUser : keyCloakUsers) {
			ZcpUser user = new ZcpUser();
			user.setId(cloakUser.getId());
			user.setUsername(cloakUser.getUsername());
			user.setEmail(cloakUser.getEmail());
			user.setLastName(cloakUser.getLastName());
			user.setFirstName(cloakUser.getFirstName());
			user.setCreatedDate(new Date(cloakUser.getCreatedTimestamp()));
			user.setEnabled(cloakUser.isEnabled());

			users.add(user);
		}

		Map<String, V1ClusterRoleBinding> mappedClusterRoleBindings = null;
		try {
			mappedClusterRoleBindings = getMappedClusterRoleBindings();
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.MAPPED_CLUSTER_ROLE_BINDINGS_ERROR, e);
		}

		Map<String, List<V1RoleBinding>> mappedRoleBindings = null;
		try {
			mappedRoleBindings = getMappedRoleBindings();
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.MAPPED_ROLE_BINDINGS_ERROR, e);
		}

		for (ZcpUser user : users) {
			List<V1RoleBinding> userRoleBindins = mappedRoleBindings.get(user.getUsername());
			if (userRoleBindins != null) {
				user.setUsedNamespace(userRoleBindins.size());
			}
			V1ClusterRoleBinding userClusterRoleBinding = mappedClusterRoleBindings.get(user.getUsername());
			if (userClusterRoleBinding != null) {
				user.setClusterRole(ClusterRole.getClusterRole(userClusterRoleBinding.getRoleRef().getName()));
			}
		}

		return new ZcpUserList(users);
	}

	public ZcpUser getUser(String id) throws ZcpException {
		UserRepresentation userRepresentation = null;
		ZcpUser zcpUser = null;
		try {
			userRepresentation = keyCloakManager.getUser(id);
		} catch (KeyCloakException e) {
			throw new ZcpException(ZcpErrorCode.GET_USER_ERROR, e);
		}

		logger.debug("keyclock user info - {}", userRepresentation);

		zcpUser = convertUser(userRepresentation);
		String username = zcpUser.getUsername();

		V1ClusterRoleBinding userClusterRoleBinding = null;
		try {
			userClusterRoleBinding = kubeRbacAuthzManager.getClusterRoleBindingByUsername(username);
		} catch (ApiException e) {
			//e.printStackTrace();
			//throw new ZcpException("ZCP-0001", e.getMessage());
			// we can ignore this case becuase the user registered by himself the clusterrole does not exist yet
		}

		if (userClusterRoleBinding == null) {
			// If user registered by himself, the clusterrolebindings may not exist before
			// cluster-admin confirms the user.
			logger.debug("The clusterrolebinding of user({}) does not exist yet", username);
			zcpUser.setClusterRole(ClusterRole.NONE);
		} else {
			zcpUser.setClusterRole(ClusterRole.getClusterRole(userClusterRoleBinding.getRoleRef().getName()));
		}

		List<String> userNamespaces = getNamespace(username, zcpUser.getClusterRole());
		zcpUser.setNamespaces(userNamespaces);
		zcpUser.setUsedNamespace(userNamespaces.size());

		return zcpUser;
	}

	public List<String> getNamespace(String username, ClusterRole role) throws ZcpException {
		UserRepresentation userRepresentation = null;
		try {
			userRepresentation = keyCloakManager.getUserFromName(username).toRepresentation();
		} catch (KeyCloakException e) {
			throw new ZcpException(ZcpErrorCode.GET_USER_ERROR, e);
		}

		// Response namespaces for ecah ClusterRole
		List<String> userNamespaces = new ArrayList<>();

		boolean supperAdmin = Optional.ofNullable(userRepresentation.getAttributes())
				.map(attr -> attr.get("superAdmin"))
				.filter(v -> !v.isEmpty())
				.map(v -> Boolean.parseBoolean(v.get(0)))
				.orElse(false);

		if (supperAdmin && ClusterRole.CLUSTER_ADMIN == role) {
			try {
				ServiceAccountApiKeyHolder.instance().setToken(username);
				V1NamespaceList all = kubeResourceManager.getList("", "namespace");
				List<V1Namespace> items = all.getItems();
				if (all != null && !items.isEmpty()) {
					for (V1Namespace ns : items) {
						userNamespaces.add(ns.getMetadata().getName());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (ClusterRole.CLUSTER_ADMIN == role) {
			try {
				List<V1Namespace> all = kubeCoreManager.getNamespaceList().getItems();
				if (all != null && !all.isEmpty()) {
					for (V1Namespace ns : all) {
						userNamespaces.add(ns.getMetadata().getName());
					}
				}
			} catch (ApiException e) {}
		} else {
			try {
				List<V1RoleBinding> userRoleBindings = kubeRbacAuthzManager.getRoleBindingListByUsername(username).getItems();
				if (userRoleBindings != null && !userRoleBindings.isEmpty()) {
					for (V1RoleBinding roleBinding : userRoleBindings) {
						userNamespaces.add(roleBinding.getMetadata().getNamespace());
					}
				}
			} catch (ApiException e) {
				//e.printStackTrace();
				//throw new ZcpException("ZCP-0001", e.getMessage());
				// we can ignore this case becase the user registered by himself the clusterrole does not exist yet
			}
		}

		return userNamespaces;
	}

	public void createUser(MemberVO user) throws ZcpException {
		// 1. create service account
		V1ServiceAccountList serviceAccountList = null;
		try {
			serviceAccountList = kubeCoreManager.getServiceAccountListByUsername(zcpSystemNamespace,
					user.getUsername());
		} catch (ApiException e) {
			// ignore
		}

		if (serviceAccountList != null) {
			V1Status status = null;
			try {
				status = kubeCoreManager.deleteServiceAccountListByUsername(zcpSystemNamespace, user.getUsername());
			} catch (ApiException e) {
				throw new ZcpException(ZcpErrorCode.DELETE_SERVICE_ACCOUNT_LIST_BY_USERNAME_ERROR, e);
			}

			logger.debug("The serviceaccounts of user({}) have been removed. {}", user.getUsername(),
					status.getMessage());
		}

		V1ServiceAccount serviceAccount = getServiceAccount(user.getUsername());

		try {
			serviceAccount = kubeCoreManager.createServiceAccount(zcpSystemNamespace, serviceAccount);
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.CREATE_SERVICE_ACCOUNT_ERROR, e);
		}

		// 2. create clusterRolebindinding
		V1ClusterRoleBindingList clusterRoleBindingList = null;
		try {
			clusterRoleBindingList = kubeRbacAuthzManager.getClusterRoleBindingListByUsername(user.getUsername());
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.CLUSTER_ROLE_BINDING_LIST_BY_USERNAME_ERROR, e);
		}

		if (clusterRoleBindingList != null) {
			V1Status status = null;
			try {
				status = kubeRbacAuthzManager.deleteClusterRoleBindingByUsername(user.getUsername());
			} catch (ApiException e) {
				throw new ZcpException(ZcpErrorCode.DELETE_CLUSTER_ROLE_BINDING_BY_USERNAME_ERROR, e);
			}

			logger.debug("The clusterRoleBindings of user({}) have been removed. {}", user.getUsername(),
					status.getMessage());
		}

		V1ClusterRoleBinding clusterRoleBinding = getClusterRoleBinding(user.getUsername(), user.getClusterRole());
		try {
			clusterRoleBinding = kubeRbacAuthzManager.createClusterRoleBinding(clusterRoleBinding);
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.CREATE_CLUSTER_ROLE_BINDING_ERROR, e);
		}

		// 3. create keycloak user
		user.setEnabled(Boolean.TRUE);
		UserRepresentation userRepresentation = getKeyCloakUser(null, user, new UserRepresentation());

		keyCloakManager.createUser(userRepresentation);
	}

	public void updateUser(String id, MemberVO user) throws ZcpException {
	    try {
		    UserRepresentation userRepresentation = keyCloakManager.getUser(id);
		    
			keyCloakManager.editUser(getKeyCloakUser(id, user, userRepresentation));
		} catch (KeyCloakException e) {
			throw new ZcpException(ZcpErrorCode.EDIT_USER_ERROR, e);
		}
	}

	public void deleteUser(String id) throws ZcpException {
		ZcpUser zcpUser = getUser(id);

		String username = zcpUser.getUsername();

		// delete service account
		try {
			kubeCoreManager.deleteServiceAccountListByUsername(zcpSystemNamespace, username);
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.DELETE_SERVICE_ACCOUNT_LIST_BY_USERNAME_ERROR, e);
		}

		// delete clusterrolebinding
		try {
			kubeRbacAuthzManager.deleteClusterRoleBindingByUsername(username);
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.DELETE_CLUSTER_ROLE_BINDING_BY_USERNAME_ERROR, e);
		}

		// delete rolebindings
		List<String> userNamespaces = zcpUser.getNamespaces();
		if (userNamespaces != null && !userNamespaces.isEmpty()) {
			for (String namespace : userNamespaces) {
				try {
					kubeRbacAuthzManager.deleteRoleBindingListByUsername(namespace, username);
				} catch (ApiException e) {
					throw new ZcpException(ZcpErrorCode.DELETE_ROLE_BINDING_LIST_BY_USERNAME_ERROR, e);
				}
			}
		}

		// delete keycloak user
		try {
			keyCloakManager.deleteUser(id);
		} catch (KeyCloakException e) {
			throw new ZcpException(ZcpErrorCode.DELETE_ROLE_BINDING_LIST_BY_USERNAME_ERROR, e);
		}
	}

	public void updateUserClusterRole(String id, UpdateClusterRoleVO vo) throws ZcpException {
		UserRepresentation userRepresentation = null;
		try {
			userRepresentation = keyCloakManager.getUser(id);
		} catch (KeyCloakException e) {
			throw new ZcpException(ZcpErrorCode.GET_USER_ERROR, e);
		}

		String username = userRepresentation.getUsername();

		// should check the service account exist or not
		// if user created by himself, the service account may not exist
		// so should create the service account
		V1ServiceAccountList serviceAccounts = null;
		try {
			serviceAccounts = kubeCoreManager.getServiceAccountListByUsername(zcpSystemNamespace, username);
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.SERVICE_ACCOUNT_LIST_BY_USERNAME_ERROR, e);
		}

		if (serviceAccounts == null || serviceAccounts.getItems() == null || serviceAccounts.getItems().isEmpty()) {
			V1ServiceAccount serviceAccount = getServiceAccount(username);
			try {
				kubeCoreManager.createServiceAccount(zcpSystemNamespace, serviceAccount);
			} catch (ApiException e) {
				throw new ZcpException(ZcpErrorCode.SERVICE_ACCOUNT_ERROR, e);
			}
		}

		// delete clusterRolebindinding
		V1ClusterRoleBindingList clusterRoleBindingList = null;
		try {
			clusterRoleBindingList = kubeRbacAuthzManager.getClusterRoleBindingListByUsername(username);
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.CLUSTER_ROLE_BINDING_LIST_BY_USERNAME_ERROR, e);
		}

		if (clusterRoleBindingList != null) {
			V1Status status = null;
			try {
				status = kubeRbacAuthzManager.deleteClusterRoleBindingByUsername(username);
			} catch (ApiException e) {
				throw new ZcpException(ZcpErrorCode.DELETE_CLUSTER_ROLE_BINDING_BY_USERNAME_ERROR, e);
			}

			logger.debug("The clusterRoleBindings of user({}) have been removed. {}", username, status.getMessage());
		}

		// create new clusterRolebindinding
		V1ClusterRoleBinding clusterRoleBinding = getClusterRoleBinding(username, vo.getClusterRole());
		try {
			clusterRoleBinding = kubeRbacAuthzManager.createClusterRoleBinding(clusterRoleBinding);
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.CREATE_CLUSTER_ROLE_BINDING_ERROR, e);
		}
		
		// create a rolebinding of default namespace
		// doesn't create default namespace rolebiding 2018-10-04
//		createDefaultNamespaceRolebinding(userRepresentation);
		
		if(clusterRoleBindingList != null && !clusterRoleBindingList.getItems().isEmpty()) {
			V1ClusterRoleBinding crb = clusterRoleBindingList.getItems().get(0);
			String oldRole = crb.getRoleRef().getName();
			
			keycloakService.deleteClusterRoles(username, ClusterRole.getClusterRole(oldRole));
			keycloakService.addClusterRoles(username, vo.getClusterRole());
		}
	}

	@SuppressWarnings("unused")
	private void createDefaultNamespaceRolebinding(UserRepresentation userRepresentation) throws ZcpException {
		ZcpUser zcpUser = convertUser(userRepresentation);
		String defaultNamespace = "default"; // this is a default namespace of k8s
		if (StringUtils.isEmpty(zcpUser.getDefaultNamespace())) {
			// create rolebinding
			V1RoleBinding roleBinding = makeRoleBinding(defaultNamespace, zcpUser.getUsername());
			try {
				roleBinding = kubeRbacAuthzManager.createRoleBinding(defaultNamespace, roleBinding);
			} catch (ApiException e) {
				throw new ZcpException(ZcpErrorCode.CREATE_ROLE_BINDING_ERROR, e);
			}
			zcpUser.setDefaultNamespace(defaultNamespace);

			// update user's default namespace
			List<String> defaultNamespaces = new ArrayList<>();
			defaultNamespaces.add(zcpUser.getDefaultNamespace());

			Map<String, List<String>> attributes = new HashMap<>();
			attributes.put(KeyCloakManager.DEFAULT_NAMESPACE_ATTRIBUTE_KEY, defaultNamespaces);

			userRepresentation.setAttributes(attributes);
			
			try {
				keyCloakManager.editUser(userRepresentation);
			} catch (KeyCloakException e) {
				throw new ZcpException(ZcpErrorCode.EDIT_USER_ERROR, e);
			}
			
		}
	}
	
	
	private V1RoleBinding makeRoleBinding(String namespace, String username) {
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
		roleRef.setName(ClusterRole.VIEW.getRole());

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



	public void updateUserPassword(String id, UpdatePasswordVO vo) throws ZcpException {
		// TODO check current password
		try {
			keyCloakManager.getAccessToken(id, vo.getCurrentPassword());
			keyCloakManager.editUserPassword(id, getCredentialRepresentation(vo.getNewPassword(), Boolean.FALSE));
		} catch (KeyCloakException e) {
			throw new ZcpException(ZcpErrorCode.EDIT_USER_PASSWORD_ERROR, e);
		}
	}

	public void resetUserPassword(String id, ResetPasswordVO vo) throws ZcpException {
		try {
			keyCloakManager.editUserPassword(id, getCredentialRepresentation(vo.getPassword(), vo.getTemporary()));
		} catch (KeyCloakException e) {
			throw new ZcpException(ZcpErrorCode.EDIT_USER_PASSWORD_ERROR, e);
		}
	}

	public V1RoleBindingList getUserRoleBindings(String id) throws ZcpException {
		ZcpUser zcpUser = getUser(id);
		V1RoleBindingList roleBindingList = null;
		try {
			roleBindingList = kubeRbacAuthzManager.getRoleBindingListByUsername(zcpUser.getUsername());
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.ROLE_BINDING_LIST_BY_USERNAME_ERROR, e);
		}

		return roleBindingList;
	}

	public void resetUserCredentials(String id, ResetCredentialVO resetCredentialVO) throws ZcpException {
		try {
			keyCloakManager.resetUserCredentials(id, resetCredentialVO.getActions());
		} catch (KeyCloakException e) {
			throw new ZcpException(ZcpErrorCode.RESET_USER_CREDENTIALS_ERROR, e);
		}
	}

	public void deleteOtpPassword(String id) throws ZcpException {
		try {
			keyCloakManager.deleteUserOtpPassword(id);
		} catch (KeyCloakException e) {
			throw new ZcpException(ZcpErrorCode.DELETE_USER_OTP_PASSWORD_ERROR, e);
		}
	}

	public void enableOtpPassword(String id) throws ZcpException {
		try {
			keyCloakManager.enableUserOtpPassword(id);
		} catch (KeyCloakException e) {
			throw new ZcpException(ZcpErrorCode.ENABLE_USER_OTP_PASSWORD_ERROR, e);
		}

	}

	public void logout(String id) throws ZcpException {
		try {
			keyCloakManager.logout(id);
		} catch (KeyCloakException e) {
			throw new ZcpException(ZcpErrorCode.LOGOUT_ERROR, e);
		}
	}

	public ZcpKubeConfig getKubeConfig(String id, String namespace) throws ZcpException {
		UserRepresentation userRepresentation = null;
		try {
			userRepresentation = keyCloakManager.getUser(id);
		} catch (KeyCloakException e) {
			throw new ZcpException(ZcpErrorCode.GET_USER_ERROR, e);
		}

		String username = userRepresentation.getUsername();
		String serviceAccountName = ResourcesNameManager.getServiceAccountName(username);
		V1ServiceAccount serviceAccount = null;
		try {
			serviceAccount = kubeCoreManager.getServiceAccount(zcpSystemNamespace, serviceAccountName);
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.SERVICE_ACCOUNT_ERROR, e);
		}

		List<V1ObjectReference> secrets = serviceAccount.getSecrets();
		V1ObjectReference objectReference = secrets.get(0);
		V1Secret secret = null;
		try {
			// objectReference.getNamespace() is null, so shoud use zcpSystemNamespace
			secret = kubeCoreManager.getSecret(zcpSystemNamespace, objectReference.getName());
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.GET_SECRET_ERROR, e);
		}

		String caCrt = new String(Base64.getEncoder().encode(secret.getData().get("ca.crt")));
		String token = new String(secret.getData().get("token"));

		ZcpKubeConfig config = generateZcpKubeConfig(kubeApiServerEndpoint, namespace, userRepresentation.getEmail(),
				caCrt, token);

		return config;
	}

	public void resetUserServiceAccount(String id) throws ZcpException {
		// 1. create service account
		UserRepresentation userRepresentation = null;
		try {
			userRepresentation = keyCloakManager.getUser(id);
		} catch (KeyCloakException e) {
			throw new ZcpException(ZcpErrorCode.GET_USER_ERROR, e);
		}

		String username = userRepresentation.getUsername();

		V1ServiceAccountList serviceAccountList = null;
		try {
			serviceAccountList = kubeCoreManager.getServiceAccountListByUsername(zcpSystemNamespace, username);
		} catch (ApiException e) {
			// ignore
		}

		if (serviceAccountList != null) {
			V1Status status = null;
			try {
				status = kubeCoreManager.deleteServiceAccountListByUsername(zcpSystemNamespace, username);
			} catch (ApiException e) {
				throw new ZcpException(ZcpErrorCode.DELETE_SERVICE_ACCOUNT_LIST_BY_USERNAME_ERROR, e);
			}

			logger.debug("The serviceaccounts of user({}) have been removed. {}", username, status.getMessage());
		}

		V1ServiceAccount serviceAccount = getServiceAccount(username);

		try {
			serviceAccount = kubeCoreManager.createServiceAccount(zcpSystemNamespace, serviceAccount);
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.CREATE_SERVICE_ACCOUNT_ERROR, e);
		}

	}

	private Map<String, List<V1RoleBinding>> getMappedRoleBindings() throws ApiException {
		List<V1RoleBinding> allRoleBindings = kubeRbacAuthzManager.getRoleBindingListAllNamespaces().getItems();
		Map<String, List<V1RoleBinding>> map = new HashMap<>();
		for (V1RoleBinding roleBinding : allRoleBindings) {
			String username = roleBinding.getMetadata().getLabels()
					.get(ResourcesLabelManager.SYSTEM_USERNAME_LABEL_NAME);
			List<V1RoleBinding> userRoleBindings = map.get(username);
			if (userRoleBindings == null) {
				userRoleBindings = new ArrayList<>();
				userRoleBindings.add(roleBinding);
				map.put(username, userRoleBindings);
			} else {
				map.get(username).add(roleBinding);
			}
		}

		return map;
	}

	private Map<String, V1ClusterRoleBinding> getMappedClusterRoleBindings() throws ApiException {
		List<V1ClusterRoleBinding> clusterRoleBindings = kubeRbacAuthzManager.getClusterRoleBindingList().getItems();

		Map<String, V1ClusterRoleBinding> map = new HashMap<>();
		for (V1ClusterRoleBinding clusterRoleBinding : clusterRoleBindings) {
			String username = clusterRoleBinding.getMetadata().getLabels()
					.get(ResourcesLabelManager.SYSTEM_USERNAME_LABEL_NAME);
			map.put(username, clusterRoleBinding);
		}

		return map;
	}

	@SuppressWarnings("deprecation")
	private ZcpUser convertUser(UserRepresentation userRepresentation) {
		ZcpUser user = new ZcpUser();
		user.setId(userRepresentation.getId());
		user.setFirstName(userRepresentation.getFirstName());
		user.setLastName(userRepresentation.getLastName());
		user.setEmail(userRepresentation.getEmail());
		user.setEnabled(userRepresentation.isEnabled());
		user.setUsername(userRepresentation.getUsername());
		user.setCreatedDate(new Date(userRepresentation.getCreatedTimestamp()));
		user.setEmailVerified(userRepresentation.isEmailVerified());
		user.setTotp(userRepresentation.isTotp());
		Map<String, List<String>> attributes = userRepresentation.getAttributes();
		
		if (attributes != null) {
		    List<String> defaultNamespaces = attributes.get(KeyCloakManager.DEFAULT_NAMESPACE_ATTRIBUTE_KEY);
			if (defaultNamespaces != null && !defaultNamespaces.isEmpty()) {
				user.setDefaultNamespace(defaultNamespaces.get(0));
			}
			
			/* zcp-1.1 add */
			List<String> zdbAdmin = attributes.get(KeyCloakManager.ZDB_ADMIN_ATTRIBUTE_KEY);
			if (zdbAdmin != null && !zdbAdmin.isEmpty()) {
                user.setZdbAdmin(zdbAdmin.get(0).equals(Boolean.TRUE.toString()) ? true : false);
            } else {
                user.setZdbAdmin(Boolean.FALSE);
            }
		} else {
		    user.setZdbAdmin(Boolean.FALSE);
		}

		List<String> requiredActions = userRepresentation.getRequiredActions();
		if (requiredActions != null && !requiredActions.isEmpty()) {
			List<CredentialActionType> zcpUserRequiredActions = new ArrayList<>();
			for (String action : requiredActions) {
				zcpUserRequiredActions.add(CredentialActionType.valueOf(action));
			}
			user.setRequiredActions(zcpUserRequiredActions);
		}

		return user;
	}

	private UserRepresentation getKeyCloakUser(String id, MemberVO user, UserRepresentation userRepresentation) {
		userRepresentation.setId(id);
		userRepresentation.setFirstName(user.getFirstName());
		userRepresentation.setLastName(user.getLastName());
		userRepresentation.setEmail(user.getEmail());
		userRepresentation.setUsername(user.getUsername());
		userRepresentation.setEnabled(user.getEnabled());
		userRepresentation.setEmailVerified(user.getEmailVerified());

		if (StringUtils.isNotEmpty(user.getDefaultNamespace())) {
			List<String> defaultNamespaces = new ArrayList<>();
			defaultNamespaces.add(user.getDefaultNamespace());
	
			Map<String, List<String>> attributes = ObjectUtils.defaultIfNull(userRepresentation.getAttributes(), new HashMap<String, List<String>>());
			attributes.put(KeyCloakManager.DEFAULT_NAMESPACE_ATTRIBUTE_KEY, defaultNamespaces);

			userRepresentation.setAttributes(attributes);
		}

		List<CredentialActionType> userRequiredActions = user.getRequiredActions();
		List<String> requiredActions = new ArrayList<>();
		if (userRequiredActions != null && !userRequiredActions.isEmpty()) {
			for (CredentialActionType type : userRequiredActions) {
				requiredActions.add(type.name());
			}
		}

		userRepresentation.setRequiredActions(requiredActions);

		return userRepresentation;
	}

	private V1ServiceAccount getServiceAccount(String username) {
		String serviceAccountName = ResourcesNameManager.getServiceAccountName(username);
		V1ObjectMeta metadata = new V1ObjectMeta();
		metadata.setName(serviceAccountName);
		metadata.setNamespace(zcpSystemNamespace);
		metadata.setLabels(ResourcesLabelManager.getSystemUsernameLabels(username));

		V1ServiceAccount serviceAccount = new V1ServiceAccount();
		serviceAccount.setMetadata(metadata);

		return serviceAccount;
	}

	private V1ClusterRoleBinding getClusterRoleBinding(String username, ClusterRole clusterRole) {
		String serviceAccountName = ResourcesNameManager.getServiceAccountName(username);
		String clusterRoleBindingName = ResourcesNameManager.getClusterRoleBindingName(username);

		V1ObjectMeta metadata = new V1ObjectMeta();
		metadata.setName(clusterRoleBindingName);
		metadata.setNamespace(zcpSystemNamespace);
		metadata.setLabels(ResourcesLabelManager.getSystemUsernameLabels(username));

		V1Subject subject = new V1Subject();
		subject.setKind("ServiceAccount");
		subject.setName(serviceAccountName);
		subject.setNamespace(zcpSystemNamespace);

		List<V1Subject> subjects = new ArrayList<V1Subject>();
		subjects.add(subject);

		V1RoleRef roleRef = new V1RoleRef();
		roleRef.setApiGroup("rbac.authorization.k8s.io");
		roleRef.setKind("ClusterRole");
		roleRef.setName(clusterRole.getRole());

		V1ClusterRoleBinding clusterRoleBinding = new V1ClusterRoleBinding();
		clusterRoleBinding.setMetadata(metadata);
		clusterRoleBinding.setRoleRef(roleRef);
		clusterRoleBinding.setSubjects(subjects);

		return clusterRoleBinding;
	}

	private CredentialRepresentation getCredentialRepresentation(String password, Boolean temporary) {
		CredentialRepresentation credentail = new CredentialRepresentation();
		credentail.setType(CredentialRepresentation.PASSWORD);
		credentail.setValue(password);
		credentail.setTemporary(temporary);
		return credentail;
	}

	private ZcpKubeConfig generateZcpKubeConfig(String apiServerEndpoint, String namespace, String email, String caCrt,
			String token) {
		ZcpKubeConfig config = new ZcpKubeConfig();

		ClusterInfo clusterInfo = config.new ClusterInfo();
		Cluster cluster = clusterInfo.new Cluster();
		cluster.setCertificateAuthorityData(caCrt);
		cluster.setServer(apiServerEndpoint);
		clusterInfo.setName("zcp-cluster");
		clusterInfo.setCluster(cluster);

		UserInfo userInfo = config.new UserInfo();
		User user = userInfo.new User();
		user.setToken(token);
		userInfo.setName(email);
		userInfo.setUser(user);

		ContextInfo contextInfo = config.new ContextInfo();
		Context context = contextInfo.new Context();
		context.setCluster(clusterInfo.getName());
		context.setUser(userInfo.getName());
		context.setNamespace(namespace);
		contextInfo.setContext(context);
		contextInfo.setName("zcp-context");

		config.setApiVersion("v1");
		config.setKind("Config");
		config.setCurrentContext(contextInfo.getName());

		config.getClusters().add(clusterInfo);
		config.getUsers().add(userInfo);
		config.getContexts().add(contextInfo);

		return config;
	}
	
	public void updateUserAttribute(String id, UserAttributeVO vo) throws ZcpException {
	    try {
            keyCloakManager.updateUserAttribute(id, vo.getKey(), vo.getValue());
        } catch (KeyCloakException e) {
            throw new ZcpException(ZcpErrorCode.EDIT_ATTRIBUTE_ERROR, e);
        }
	}
	
	public UserAttributeVO getUserAttribute(String id, String key) throws ZcpException {
	    UserAttributeVO userAttributeVO = null;
	    
	    try {
            UserRepresentation userRepresentation = keyCloakManager.getUser(id);
            
            Map<String, List<String>> attributes = userRepresentation.getAttributes();
            if (attributes != null) {
                List<String> attribute = attributes.get(key);
                
                if (attribute != null && !attribute.isEmpty()) {
                    userAttributeVO = new UserAttributeVO(key, attribute.get(0));
                }
            }
        } catch (KeyCloakException e) {
            throw new ZcpException(ZcpErrorCode.GET_ATTRIBUTE_ERROR, e);
        }
	    
	    return userAttributeVO;
	}

}
