package com.skcc.cloudz.zcp.iam.manager;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.skcc.cloudz.zcp.iam.common.model.V1alpha1NodeMetricList;
import com.skcc.cloudz.zcp.iam.manager.api.MetricV1alph1Api;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.util.Config;

@Component
public class KubeMetricManager {

	private final Logger logger = (Logger) LoggerFactory.getLogger(KubeMetricManager.class);

	private ApiClient client;

	private MetricV1alph1Api api;

	@Value("${kube.client.api.output.pretty}")
	private String pretty;
	
	@Value("${kube.server.metrics.type}")
	private String kubeMetric;

	public KubeMetricManager() throws IOException {
		client = Config.defaultClient();
		Configuration.setDefaultApiClient(client);
		api = new MetricV1alph1Api(this.client);

		logger.debug("KubeMetricManager is initialized");
		logger.debug("kubernetes metrics type is "+kubeMetric+" (default is metrics-server)");
	}

	public V1alpha1NodeMetricList listNodeMetrics() throws ApiException {
		return api.listNodeMetrics(null, pretty, null, null, null, null, null, null, null, null, kubeMetric);
	}

}
