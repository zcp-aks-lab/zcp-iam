package com.skcc.cloudz.zcp.metric.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skcc.cloudz.zcp.common.model.V1alpha1NodeMetricList;
import com.skcc.cloudz.zcp.common.model.ZcpNamespaceList;
import com.skcc.cloudz.zcp.common.model.ZcpNodeList;
import com.skcc.cloudz.zcp.common.vo.Response;
import com.skcc.cloudz.zcp.metric.service.MetricService;
import com.skcc.cloudz.zcp.metric.vo.DeploymentsStatusMetricsVO;
import com.skcc.cloudz.zcp.metric.vo.NodesStatusMetricsVO;
import com.skcc.cloudz.zcp.metric.vo.PodsStatusMetricsVO;

@Configuration
@RestController
@RequestMapping("/iam")
public class MetricController {

	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(MetricController.class);

	@Autowired
	private MetricService metricService;

	@RequestMapping(value = "/metrics/namespaces", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<ZcpNamespaceList> getNamespaceList(@RequestParam(required = true, value = "userId") String userId)
			throws Exception {
		Response<ZcpNamespaceList> response = new Response<>();
		response.setData(metricService.getNamespaceList(userId));

		return response;
	}

	@RequestMapping(value = "/metrics/nodes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<ZcpNodeList> getNodeList() throws Exception {
		Response<ZcpNodeList> response = new Response<>();
		response.setData(metricService.getNodeList());

		return response;
	}
	
	@RequestMapping(value = "/metrics/pods/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<PodsStatusMetricsVO> getPodsStatus(@RequestParam(required = false, value = "namespace") String namespace) throws Exception {
		Response<PodsStatusMetricsVO> response = new Response<>();
		response.setData(metricService.getPodsStatusMetrics(namespace));

		return response;
	}

	@RequestMapping(value = "/metrics/deployments/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<DeploymentsStatusMetricsVO> getDeploymentsStatus(@RequestParam(required = false, value = "namespace") String namespace) throws Exception {
		Response<DeploymentsStatusMetricsVO> response = new Response<>();
		response.setData(metricService.getDeploymentsStatusMetrics(namespace));

		return response;
	}

	@RequestMapping(value = "/metrics/nodes/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<NodesStatusMetricsVO> getNodesStatus() throws Exception {
		Response<NodesStatusMetricsVO> response = new Response<>();
		response.setData(metricService.getNodesStatusMetrics());

		return response;
	}

	@RequestMapping(value = "/metrics/memory/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<V1alpha1NodeMetricList> getMemoryStatus() throws Exception {
		Response<V1alpha1NodeMetricList> response = new Response<>();
		response.setData(metricService.getNodeMetrics());
	
		return response;
	}

	@RequestMapping(value = "/metrics/cpu/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<V1alpha1NodeMetricList> getCPUStatus() throws Exception {
		Response<V1alpha1NodeMetricList> response = new Response<>();
		response.setData(metricService.getNodeMetrics());
	
		return response;
	}
}
