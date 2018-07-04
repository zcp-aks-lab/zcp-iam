package com.skcc.cloudz.zcp.metric.service;

import java.math.BigDecimal;
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

import com.skcc.cloudz.zcp.common.exception.KeyCloakException;
import com.skcc.cloudz.zcp.common.exception.ZcpException;
import com.skcc.cloudz.zcp.common.model.ClusterRole;
import com.skcc.cloudz.zcp.common.model.DeploymentStatus;
import com.skcc.cloudz.zcp.common.model.DeploymentStatusMetric;
import com.skcc.cloudz.zcp.common.model.NodeStatus;
import com.skcc.cloudz.zcp.common.model.NodeStatusMetric;
import com.skcc.cloudz.zcp.common.model.PodStatus;
import com.skcc.cloudz.zcp.common.model.PodStatusMetric;
import com.skcc.cloudz.zcp.common.model.V1alpha1NodeMetricList;
import com.skcc.cloudz.zcp.common.model.ZcpNamespace;
import com.skcc.cloudz.zcp.common.model.ZcpNamespace.NamespaceStatus;
import com.skcc.cloudz.zcp.common.model.ZcpNamespaceList;
import com.skcc.cloudz.zcp.common.model.ZcpNode;
import com.skcc.cloudz.zcp.common.model.ZcpNodeList;
import com.skcc.cloudz.zcp.manager.KeyCloakManager;
import com.skcc.cloudz.zcp.manager.KubeAppsManager;
import com.skcc.cloudz.zcp.manager.KubeCoreManager;
import com.skcc.cloudz.zcp.manager.KubeMetricManager;
import com.skcc.cloudz.zcp.manager.KubeRbacAuthzManager;
import com.skcc.cloudz.zcp.metric.vo.DeploymentsStatusMetricsVO;
import com.skcc.cloudz.zcp.metric.vo.NodesStatusMetricsVO;
import com.skcc.cloudz.zcp.metric.vo.PodsStatusMetricsVO;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.V1ClusterRoleBinding;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1NamespaceList;
import io.kubernetes.client.models.V1Node;
import io.kubernetes.client.models.V1NodeCondition;
import io.kubernetes.client.models.V1NodeList;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.models.V1ResourceQuota;
import io.kubernetes.client.models.V1ResourceQuotaList;
import io.kubernetes.client.models.V1ResourceQuotaStatus;
import io.kubernetes.client.models.V1RoleBinding;
import io.kubernetes.client.models.V1beta2Deployment;
import io.kubernetes.client.models.V1beta2DeploymentCondition;
import io.kubernetes.client.models.V1beta2DeploymentList;

@Service
public class MetricService {

	private final Logger logger = LoggerFactory.getLogger(MetricService.class);

	@Autowired
	private KeyCloakManager keyCloakManager;

	@Autowired
	private KubeMetricManager kubeMetircManager;

	@Autowired
	private KubeCoreManager kubeCoreManager;

	@Autowired
	private KubeRbacAuthzManager kubeRbacAuthzManager;

	@Autowired
	private KubeAppsManager kubeAppsManager;

	@Value("${zcp.kube.namespace}")
	private String zcpSystemNamespace;

	@Value("${kube.server.apiserver.endpoint}")
	private String kubeApiServerEndpoint;

	public enum MemoryDisplayFormat {
		MI, GI
	}

	public V1alpha1NodeMetricList getNodeMetrics() throws ZcpException {
		V1alpha1NodeMetricList list = null;
		try {
			list = kubeMetircManager.listNodeMetrics();
		} catch (ApiException e) {
			e.printStackTrace();
			throw new ZcpException("ZCP-009", e.getMessage());
		}

		logger.debug("Node metrics have been received");

		return list;
	}

	public ZcpNodeList getNodeList() throws ZcpException {
		V1NodeList nodeList = null;

		try {
			nodeList = kubeCoreManager.getNodeList();
		} catch (ApiException e) {
			e.printStackTrace();
			throw new ZcpException("ZCP-009", e.getMessage());
		}

		List<V1Node> nodes = nodeList.getItems();
		List<ZcpNode> zcpNodes = new ArrayList<>();
		for (V1Node node : nodes) {
			String nodeName = node.getMetadata().getName();
			Map<String, String> labels = node.getMetadata().getLabels();
			Map<String, Quantity> allocatable = node.getStatus().getAllocatable();
			BigDecimal allocatableCpu = allocatable.get("cpu").getNumber();
			BigDecimal allocatableMem = allocatable.get("memory").getNumber();

			// Map<String, Quantity> capacity = node.getStatus().getCapacity();
			logger.debug("Node name is {}", nodeName);
			logger.debug("Node role is {}", labels.get("role"));
			logger.debug("allocatable memory is {}", allocatableCpu);
			logger.debug("allocatable cpu is {}", ((Quantity) allocatable.get("memory")).getNumber());

			V1PodList pods = getPodListByNode(nodeName);

			double totalCpuRequests = 0;
			double totalMemRequests = 0;
			double totalCpuLimits = 0;
			double totalMemLimits = 0;
			for (V1Pod pod : pods.getItems()) {
				List<V1Container> containers = pod.getSpec().getContainers();
				for (V1Container container : containers) {
					Map<String, Quantity> requests = container.getResources().getRequests();
					if (requests != null) {
						Quantity cpuRequests = requests.get("cpu");
						if (cpuRequests != null) {
							logger.debug("cpu request is {}", cpuRequests.toSuffixedString());
							totalCpuRequests += cpuRequests.getNumber().doubleValue();
						}
						Quantity memoryRequests = requests.get("memory");
						if (memoryRequests != null) {
							logger.debug("memory request is {}", memoryRequests.toSuffixedString());
							totalMemRequests += memoryRequests.getNumber().doubleValue();
						}
					}

					Map<String, Quantity> limits = container.getResources().getLimits();
					if (limits != null) {
						Quantity cpulimits = limits.get("cpu");
						if (cpulimits != null) {
							logger.debug("cpu limits is {}", cpulimits.toSuffixedString());
							totalCpuLimits += cpulimits.getNumber().doubleValue();
						}
						Quantity memorylimits = limits.get("memory");
						if (memorylimits != null) {
							logger.debug("cpu limits is {}", memorylimits.toSuffixedString());
							totalMemLimits += memorylimits.getNumber().doubleValue();
						}
					}
				}
			}

			logger.debug("total cpu requests is {}", totalCpuRequests);
			logger.debug("total mem requests is {}", totalMemRequests);
			logger.debug("total cpu limits is {}", totalCpuLimits);
			logger.debug("total mem limits is {}", totalMemLimits);

			logger.debug("CPU Requests = {}, Mem Requests = {}, CPU limits = {}, Mem limits = {}",
					percent(totalCpuRequests, allocatableCpu.doubleValue()),
					percent(totalMemRequests, allocatableMem.doubleValue()),
					percent(totalCpuLimits, allocatableCpu.doubleValue()),
					percent(totalMemLimits, allocatableMem.doubleValue()));

			logger.debug("------------------------------------------------");

			ZcpNode zcpNode = new ZcpNode();
			zcpNode.setNodeName(nodeName);
			setNodeStatus(node, zcpNode);
			zcpNode.setCreationTime(new Date(node.getMetadata().getCreationTimestamp().getMillis()));
			zcpNode.setAllocatableCpu(allocatableCpu);
			zcpNode.setAllocatableCpuString(formatCpu(allocatableCpu.doubleValue()));
			zcpNode.setAllocatableMemory(allocatableMem);
			zcpNode.setAllocatableMemoryString(formatMemory(allocatableMem.doubleValue()));

			zcpNode.setCpuLimits(BigDecimal.valueOf(totalCpuLimits));
			zcpNode.setCpuLimitsString(formatCpu(totalCpuLimits));
			zcpNode.setCpuRequests(BigDecimal.valueOf(totalCpuRequests));
			zcpNode.setCpuRequestsString(formatCpu(totalCpuRequests));
			zcpNode.setMemoryLimits(BigDecimal.valueOf(totalMemLimits));
			zcpNode.setMemoryLimitsString(formatMemory(totalMemLimits));
			zcpNode.setMemoryRequests(BigDecimal.valueOf(totalMemRequests));
			zcpNode.setMemoryRequestsString(formatMemory(totalMemRequests));
			zcpNode.setCpuLimitsPercentage(percent(totalCpuLimits, allocatableCpu.doubleValue()));
			zcpNode.setCpuRequestsPercentage(percent(totalCpuRequests, allocatableCpu.doubleValue()));
			zcpNode.setMemoryLimitsPercentage(percent(totalMemLimits, allocatableMem.doubleValue()));
			zcpNode.setMemoryRequestsPercentage(percent(totalMemRequests, allocatableMem.doubleValue()));

			zcpNodes.add(zcpNode);
		}

		return new ZcpNodeList(zcpNodes);
	}

	public ZcpNamespaceList getNamespaceList(String userId) throws ZcpException {
		// check user
		UserRepresentation userRepresentation = null;
		try {
			userRepresentation = keyCloakManager.getUser(userId);
		} catch (KeyCloakException e) {
			throw new ZcpException("ZCP-0001", "The user(" + userId + ") does not exist");
		}

		String username = userRepresentation.getUsername();
		logger.debug("keyclock username is - {}", username);

		// check clusterrolebinding
		V1ClusterRoleBinding userClusterRoleBinding = null;
		try {
			userClusterRoleBinding = kubeRbacAuthzManager.getClusterRoleBindingByUsername(username);
		} catch (ApiException e2) {
			throw new ZcpException("ZCP-0001", "The clusterrolebinding of user(" + userId + ") does not exist");
		}

		String userClusterRole = userClusterRoleBinding.getRoleRef().getName();
		boolean isClusterAdmin = StringUtils.equals(userClusterRole, ClusterRole.CLUSTER_ADMIN.getRole()) ? true
				: false;
		boolean isAdmin = StringUtils.equals(userClusterRole, ClusterRole.ADMIN.getRole()) ? true : false;

		if (!isClusterAdmin && !isAdmin) {
			throw new ZcpException("ZCP-0001",
					"The user(" + userId + ") does not have a permission for namespace list");
		}

		// get namespace list of admin user
		List<String> userNamespaces = new ArrayList<>();
		if (isAdmin) {
			List<V1RoleBinding> userRoleBindings = null;
			try {
				userRoleBindings = kubeRbacAuthzManager.getRoleBindingListByUsername(username).getItems();
			} catch (ApiException e1) {
				throw new ZcpException("ZCP-0001");
			}

			if (userRoleBindings != null && !userRoleBindings.isEmpty()) {
				for (V1RoleBinding roleBinding : userRoleBindings) {
					if (roleBinding.getRoleRef().getName().equals(ClusterRole.ADMIN.getRole())) {
						userNamespaces.add(roleBinding.getMetadata().getNamespace());
					}
				}
			}
		}

		// get all namespace list
		V1NamespaceList v1NamespaceList = null;
		try {
			v1NamespaceList = kubeCoreManager.getNamespaceList();
		} catch (ApiException e) {
			logger.debug("There is no namespace");
			return new ZcpNamespaceList(new ArrayList<ZcpNamespace>());
		}

		V1ResourceQuotaList v1ResourceQuotaList = null;
		try {
			v1ResourceQuotaList = kubeCoreManager.getAllResourceQuotaList();
		} catch (ApiException e) {
			// we can ignore this case
			logger.debug("There is no resource quotas");
		}

		Map<String, V1ResourceQuota> mappedResourceQuotas = getMappedResoruceQuotaList(v1ResourceQuotaList);
		Map<String, List<V1RoleBinding>> mappedRolebindins = getMappedRoleBindings();
		List<ZcpNamespace> zcpNamespaces = new ArrayList<>();

		for (V1Namespace namespace : v1NamespaceList.getItems()) {
			ZcpNamespace zcpNamespace = new ZcpNamespace();
			zcpNamespace.setName(namespace.getMetadata().getName());
			zcpNamespace.setCreationDate(new Date(namespace.getMetadata().getCreationTimestamp().getMillis()));
			zcpNamespace.setStatus(NamespaceStatus.getNamespaceStatus(namespace.getStatus().getPhase()));
			List<V1RoleBinding> namespacedRolebinds = mappedRolebindins.get(namespace.getMetadata().getName());
			if (namespacedRolebinds != null) {
				zcpNamespace.setUserCount(namespacedRolebinds.size());
			}

			V1ResourceQuota resourceQuota = mappedResourceQuotas.get(namespace.getMetadata().getName());
			if (resourceQuota != null) {
				V1ResourceQuotaStatus status = resourceQuota.getStatus();
				Map<String, String> hard = status.getHard();
				Map<String, String> used = status.getUsed();

				logger.debug("namespace is {}", namespace.getMetadata().getName());
				logger.debug("hard is {}", hard);
				logger.debug("used is {}", used);

				BigDecimal hardRequestsCpu = Quantity.fromString(hard.get("requests.cpu")).getNumber();
				BigDecimal usedRequestsCpu = Quantity.fromString(used.get("requests.cpu")).getNumber();
				zcpNamespace.setHardCpuRequests(hardRequestsCpu);
				zcpNamespace.setUsedCpuRequests(usedRequestsCpu);
				zcpNamespace.setHardCpuRequestsString(formatCpu(hardRequestsCpu.doubleValue()));
				zcpNamespace.setUsedCpuRequestsString(formatCpu(usedRequestsCpu.doubleValue()));
				zcpNamespace.setCpuRequestsPercentage(
						percent(usedRequestsCpu.doubleValue(), hardRequestsCpu.doubleValue()));

				BigDecimal hardLimitsCpu = Quantity.fromString(hard.get("limits.cpu")).getNumber();
				BigDecimal usedLimitsCpu = Quantity.fromString(used.get("limits.cpu")).getNumber();
				zcpNamespace.setHardCpuLimits(hardLimitsCpu);
				zcpNamespace.setUsedCpuLimits(usedLimitsCpu);
				zcpNamespace.setHardCpuLimitsString(formatCpu(hardLimitsCpu.doubleValue()));
				zcpNamespace.setUsedCpuLimitsString(formatCpu(usedLimitsCpu.doubleValue()));
				zcpNamespace.setCpuLimitsPercentage(percent(usedLimitsCpu.doubleValue(), hardLimitsCpu.doubleValue()));

				BigDecimal hardRequestsMemory = Quantity.fromString(hard.get("requests.memory")).getNumber();
				BigDecimal usedRequestsMemory = Quantity.fromString(used.get("requests.memory")).getNumber();
				zcpNamespace.setHardMemoryRequests(hardRequestsMemory);
				zcpNamespace.setUsedMemoryRequests(usedRequestsMemory);
				zcpNamespace.setHardMemoryRequestsString(formatMemory(hardRequestsMemory.doubleValue()));
				zcpNamespace.setUsedMemoryRequestsString(formatMemory(usedRequestsMemory.doubleValue()));
				zcpNamespace.setMemoryRequestsPercentage(
						percent(usedRequestsMemory.doubleValue(), hardRequestsMemory.doubleValue()));

				BigDecimal hardLimitsMemory = Quantity.fromString(hard.get("limits.memory")).getNumber();
				BigDecimal usedLimitsMemory = Quantity.fromString(used.get("limits.memory")).getNumber();
				zcpNamespace.setHardMemoryLimits(hardLimitsMemory);
				zcpNamespace.setUsedMemoryLimits(usedLimitsMemory);
				zcpNamespace.setHardMemoryLimitsString(formatMemory(hardLimitsMemory.doubleValue()));
				zcpNamespace.setUsedMemoryLimitsString(formatMemory(usedLimitsMemory.doubleValue()));
				zcpNamespace.setMemoryLimitsPercentage(
						percent(usedLimitsMemory.doubleValue(), hardLimitsMemory.doubleValue()));
			}

			if (isAdmin) {
				if (userNamespaces.contains(namespace.getMetadata().getName())) {
					zcpNamespaces.add(zcpNamespace);
				}
			} else {
				zcpNamespaces.add(zcpNamespace);
			}
		}

		return new ZcpNamespaceList(zcpNamespaces);
	}

	private Map<String, V1ResourceQuota> getMappedResoruceQuotaList(V1ResourceQuotaList v1ResourceQuotaList) {
		Map<String, V1ResourceQuota> data = new HashMap<>();

		if (v1ResourceQuotaList == null) {
			return data;
		}

		List<V1ResourceQuota> resourceQuotas = v1ResourceQuotaList.getItems();
		if (resourceQuotas == null) {
			return data;
		}

		for (V1ResourceQuota quota : resourceQuotas) {
			// if namespace has quotas more than 1, the last quota is set
			data.put(quota.getMetadata().getNamespace(), quota);
		}

		return data;
	}

	private Map<String, List<V1RoleBinding>> getMappedRoleBindings() throws ZcpException {
		List<V1RoleBinding> roleBindings = null;
		try {
			roleBindings = kubeRbacAuthzManager.getRoleBindingListAllNamespaces().getItems();
		} catch (ApiException e) {
			// we can ignore this case
			logger.debug("There is no resource quotas");
		}

		Map<String, List<V1RoleBinding>> data = new HashMap<>();
		if (roleBindings != null) {
			for (V1RoleBinding roleBinding : roleBindings) {
				String namespace = roleBinding.getMetadata().getNamespace();
				List<V1RoleBinding> namespaceRoleBindings = data.get(namespace);
				if (namespaceRoleBindings == null) {
					namespaceRoleBindings = new ArrayList<>();
					namespaceRoleBindings.add(roleBinding);
					data.put(namespace, namespaceRoleBindings);
				} else {
					namespaceRoleBindings.add(roleBinding);
				}
			}
		}

		return data;
	}

	private void setNodeStatus(V1Node node, ZcpNode zcpNode) {
		List<V1NodeCondition> conditions = node.getStatus().getConditions();
		if (conditions == null)
			return;

		for (V1NodeCondition condition : conditions) {
			if (StringUtils.equals(condition.getType(), "Ready")) {
				if (StringUtils.equals(condition.getStatus(), "True")) {
					zcpNode.setStatus(NodeStatus.Ready);
				} else if (StringUtils.equals(condition.getStatus(), "False")) {
					zcpNode.setStatus(NodeStatus.NotReady);
				} else if (StringUtils.equals(condition.getStatus(), "Unknown")) {
					zcpNode.setStatus(NodeStatus.Unknown);
				}
			}
		}
	}

	public V1PodList getPodListByNode(String nodeName) throws ZcpException {
		V1PodList podList = null;

		try {
			podList = kubeCoreManager.getPodListByNode(nodeName);
		} catch (ApiException e) {
			e.printStackTrace();
			throw new ZcpException("ZCP-009", e.getMessage());
		}

		return podList;
	}

	private int percent(double usage, double sum) {
		if (usage == 0)
			return 0;
		if (sum == 0)
			return 0;

		return (int) ((usage / sum) * 100);
	}

	@SuppressWarnings("unused")
	private int percent(int usage, int sum) {
		if (usage == 0)
			return 0;
		if (sum == 0)
			return 0;

		return (int) ((usage / sum) * 100);
	}

	private String formatCpu(double value) {
		if (value == 0)
			return StringUtils.substringBefore(String.valueOf(value), ".");

		if (value < 1) {
			double formattedValue = value * 1000;
			return StringUtils.substringBefore(String.valueOf(formattedValue), ".") + "m";
		} else {
			return StringUtils.substringBefore(String.valueOf(value), ".");
		}
	}

	@SuppressWarnings("unused")
	private String formatMemory(double value, MemoryDisplayFormat mdf) {
		double formattedValue = 0l;
		if (mdf == MemoryDisplayFormat.MI) {
			formattedValue = value / 1024 / 1024;
			return StringUtils.substringBefore(String.valueOf(formattedValue), ".") + "Mi";
		} else if (mdf == MemoryDisplayFormat.GI) {
			formattedValue = value / 1024 / 1024 / 1024;
			int index = StringUtils.indexOf(String.valueOf(formattedValue), ".");
			index += 2;
			return StringUtils.substring(String.valueOf(formattedValue), index) + "Gi";
		} else {
			throw new IllegalArgumentException("Display formation is invalid");
		}
	}

	private String formatMemory(double value) {
		if (value == 0)
			return StringUtils.substringBefore(String.valueOf(value), ".");

		double formattedValue = value / 1024 / 1024;
		if (formattedValue < 1024) {
			return StringUtils.substringBefore(String.valueOf(formattedValue), ".") + "Mi";
		} else {
			formattedValue = formattedValue / 1024;
			return StringUtils.substringBefore(String.valueOf(formattedValue), ".") + "Gi";
		}
	}

	public DeploymentsStatusMetricsVO getDeploymentsStatusMetrics(String namespace) throws ZcpException {
		V1beta2DeploymentList deploymentList = null;
		try {
			deploymentList = kubeAppsManager.getDeploymentList(namespace);
		} catch (ApiException e) {
			throw new ZcpException("KK", e.getMessage());
		}

		List<V1beta2Deployment> deployments = deploymentList.getItems();
		Map<DeploymentStatus, DeploymentStatusMetric> statuesMetrics = getDeploymentsStatusMap();

		for (V1beta2Deployment deployment : deployments) {
			List<V1beta2DeploymentCondition> conditions = deployment.getStatus().getConditions();
			for (V1beta2DeploymentCondition condition : conditions) {
				if (condition.getType().equals(DeploymentStatus.STATUS_CONDITION_TYPE)) {
					if (condition.getStatus().equals("True")) {
						DeploymentStatusMetric dsm = statuesMetrics.get(DeploymentStatus.Available);
						if (dsm != null) {
							dsm.increaseCount();
						} else {
							dsm = new DeploymentStatusMetric();
							dsm.setStatus(DeploymentStatus.Available);
							dsm.setCount(1);
							statuesMetrics.put(DeploymentStatus.Available, dsm);
						}
					} else {
						DeploymentStatusMetric dsm = statuesMetrics.get(DeploymentStatus.Unavailable);
						if (dsm != null) {
							dsm.increaseCount();
						} else {
							dsm = new DeploymentStatusMetric();
							dsm.setStatus(DeploymentStatus.Unavailable);
							dsm.setCount(1);
							statuesMetrics.put(DeploymentStatus.Unavailable, dsm);
						}
					}
				}
			}
		}

		DeploymentsStatusMetricsVO vo = new DeploymentsStatusMetricsVO();
		vo.setStatuses(statuesMetrics.values().stream().collect(Collectors.toList()));
		vo.setTotalCount(deployments.size());

		return vo;
	}

	private Map<DeploymentStatus, DeploymentStatusMetric> getDeploymentsStatusMap() {
		Map<DeploymentStatus, DeploymentStatusMetric> statuesMetrics = new HashMap<>();

		for (DeploymentStatus status : DeploymentStatus.values()) {
			DeploymentStatusMetric psm = new DeploymentStatusMetric();
			psm.setStatus(status);
			psm.setCount(0);
			statuesMetrics.put(status, psm);
		}

		return statuesMetrics;
	}

	public NodesStatusMetricsVO getNodesStatusMetrics() throws ZcpException {
		V1NodeList nodeList = null;

		try {
			nodeList = kubeCoreManager.getNodeList();
		} catch (ApiException e) {
			e.printStackTrace();
			throw new ZcpException("ZCP-009", e.getMessage());
		}

		List<V1Node> nodes = nodeList.getItems();
		Map<NodeStatus, NodeStatusMetric> statuesMetrics = getNodesStatusMap();

		for (V1Node node : nodes) {
			List<V1NodeCondition> conditions = node.getStatus().getConditions();
			for (V1NodeCondition condition : conditions) {
				if (condition.getType().equals(NodeStatus.STATUS_CONDITION_TYPE)) {
					if (condition.getStatus().equals("True")) {
						NodeStatusMetric nsm = statuesMetrics.get(NodeStatus.Ready);
						if (nsm != null) {
							nsm.increaseCount();
						} else {
							nsm = new NodeStatusMetric();
							nsm.setStatus(NodeStatus.Ready);
							nsm.setCount(1);
							statuesMetrics.put(NodeStatus.Ready, nsm);
						}
					} else if (condition.getStatus().equals("False")) {
						NodeStatusMetric nsm = statuesMetrics.get(NodeStatus.NotReady);
						if (nsm != null) {
							nsm.increaseCount();
						} else {
							nsm = new NodeStatusMetric();
							nsm.setStatus(NodeStatus.NotReady);
							nsm.setCount(1);
							statuesMetrics.put(NodeStatus.NotReady, nsm);
						}
					} else {
						NodeStatusMetric nsm = statuesMetrics.get(NodeStatus.Unknown);
						if (nsm != null) {
							nsm.increaseCount();
						} else {
							nsm = new NodeStatusMetric();
							nsm.setStatus(NodeStatus.Unknown);
							nsm.setCount(1);
							statuesMetrics.put(NodeStatus.Unknown, nsm);
						}
					}
				}
			}
		}

		NodesStatusMetricsVO vo = new NodesStatusMetricsVO();
		vo.setStatuses(statuesMetrics.values().stream().collect(Collectors.toList()));
		vo.setTotalCount(nodes.size());

		return vo;
	}

	private Map<NodeStatus, NodeStatusMetric> getNodesStatusMap() {
		Map<NodeStatus, NodeStatusMetric> statuesMetrics = new HashMap<>();

		for (NodeStatus status : NodeStatus.values()) {
			NodeStatusMetric psm = new NodeStatusMetric();
			psm.setStatus(status);
			psm.setCount(0);
			statuesMetrics.put(status, psm);
		}

		return statuesMetrics;
	}

	public PodsStatusMetricsVO getPodsStatusMetrics(String namespace) throws ZcpException {
		V1PodList podList = null;

		try {
			if (StringUtils.isEmpty(namespace)) {
				podList = kubeCoreManager.getAllPodList();
			} else {
				podList = kubeCoreManager.getPodListByNamespace(namespace);
			}
		} catch (ApiException e) {
			e.printStackTrace();
			throw new ZcpException("ZCP-009", e.getMessage());
		}

		List<V1Pod> pods = podList.getItems();
		Map<PodStatus, PodStatusMetric> statuesMetrics = getPodsStatusMap();

		for (V1Pod pod : pods) {
			String phase = pod.getStatus().getPhase();

			for (PodStatus status : PodStatus.values()) {
				if (StringUtils.equals(phase, status.name())) {
					PodStatusMetric psm = statuesMetrics.get(status);
					if (psm != null) {
						psm.increaseCount();
					} else {
						psm = new PodStatusMetric();
						psm.setStatus(status);
						psm.setCount(1);
						statuesMetrics.put(status, psm);
					}
				} else {
					logger.warn("This phase("+ phase +") does not exist in PodStatus. Please check it");
				}
			}
		}

		PodsStatusMetricsVO vo = new PodsStatusMetricsVO();
		vo.setStatuses(statuesMetrics.values().stream().collect(Collectors.toList()));
		vo.setTotalCount(pods.size());

		return vo;
	}

	private Map<PodStatus, PodStatusMetric> getPodsStatusMap() {
		Map<PodStatus, PodStatusMetric> statuesMetrics = new HashMap<>();

		for (PodStatus status : PodStatus.values()) {
			PodStatusMetric psm = new PodStatusMetric();
			psm.setStatus(status);
			psm.setCount(0);
			statuesMetrics.put(status, psm);
		}

		return statuesMetrics;
	}

}
