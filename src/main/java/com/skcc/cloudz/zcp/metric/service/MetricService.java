package com.skcc.cloudz.zcp.metric.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.skcc.cloudz.zcp.common.exception.ZcpException;
import com.skcc.cloudz.zcp.common.model.V1alpha1NodeMetricList;
import com.skcc.cloudz.zcp.common.model.ZcpNode;
import com.skcc.cloudz.zcp.common.model.ZcpNodeList;
import com.skcc.cloudz.zcp.manager.KubeCoreManager;
import com.skcc.cloudz.zcp.manager.KubeMetricManager;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1Node;
import io.kubernetes.client.models.V1NodeList;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;

@Service
public class MetricService {

	private final Logger logger = LoggerFactory.getLogger(MetricService.class);

	// @Autowired
	// private KeyCloakManager keyCloakManager;

	@Autowired
	private KubeMetricManager kubeMetircManager;

	@Autowired
	private KubeCoreManager kubeCoreManager;

	@Value("${zcp.kube.namespace}")
	private String zcpSystemNamespace;

	@Value("${kube.server.apiserver.endpoint}")
	private String kubeApiServerEndpoint;

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
							totalCpuRequests += cpuRequests.getNumber().doubleValue();
						}
						Quantity memoryRequests = requests.get("memory");
						if (memoryRequests != null) {
							totalMemRequests += memoryRequests.getNumber().doubleValue();
						}
					}

					Map<String, Quantity> limits = container.getResources().getLimits();
					if (limits != null) {
						Quantity cpulimits = limits.get("cpu");
						if (cpulimits != null) {
							totalCpuLimits += cpulimits.getNumber().doubleValue();
						}
						Quantity memorylimits = limits.get("memory");
						if (memorylimits != null) {
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
			// zcpNode.setStatus(node.getStatus());
			zcpNode.setCpuLimits(BigDecimal.valueOf(totalCpuLimits));
			zcpNode.setCpuRequests(BigDecimal.valueOf(totalCpuRequests));
			zcpNode.setMemoryLimits(BigDecimal.valueOf(totalMemLimits));
			zcpNode.setMemoryRequests(BigDecimal.valueOf(totalMemRequests));
			zcpNode.setCpuLimitsPercentage(percent(totalCpuLimits, allocatableCpu.doubleValue()));
			zcpNode.setCpuRequestsPercentage(percent(totalCpuRequests, allocatableCpu.doubleValue()));
			zcpNode.setMemoryLimitsPercentage(percent(totalMemLimits, allocatableMem.doubleValue()));
			zcpNode.setMemoryRequestsPercentage(percent(totalMemRequests, allocatableMem.doubleValue()));
			
			zcpNodes.add(zcpNode);
		}

		return new ZcpNodeList(zcpNodes);
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
		return (int) ((usage / sum) * 100);
	}

}
