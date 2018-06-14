package com.skcc.cloudz.zcp.metric.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.skcc.cloudz.zcp.common.exception.ZcpException;
import com.skcc.cloudz.zcp.common.model.V1alpha1NodeMetricList;
import com.skcc.cloudz.zcp.manager.KubeMetricManager;

import io.kubernetes.client.ApiException;

@Service
public class MetricService {

	private final Logger logger = LoggerFactory.getLogger(MetricService.class);

//	@Autowired
//	private KeyCloakManager keyCloakManager;

	@Autowired
	private KubeMetricManager kubeMetircManager;

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

}
