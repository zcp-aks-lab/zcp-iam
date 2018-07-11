package com.skcc.cloudz.zcp.iam.api.metric.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skcc.cloudz.zcp.iam.api.metric.service.MetricService;
import com.skcc.cloudz.zcp.iam.api.metric.vo.ClusterStatusMetricsVO;
import com.skcc.cloudz.zcp.iam.api.metric.vo.DeploymentsStatusMetricsVO;
import com.skcc.cloudz.zcp.iam.api.metric.vo.NodesStatusMetricsVO;
import com.skcc.cloudz.zcp.iam.api.metric.vo.PodsStatusMetricsVO;
import com.skcc.cloudz.zcp.iam.api.metric.vo.UsersStatusMetricsVO;
import com.skcc.cloudz.zcp.iam.common.model.ZcpNamespaceList;
import com.skcc.cloudz.zcp.iam.common.model.ZcpNodeList;
import com.skcc.cloudz.zcp.iam.common.vo.Response;

@Configuration
@RestController
@RequestMapping("/iam")
public class MetricController {

	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(MetricController.class);

	@Autowired
	private MetricService metricService;

	@RequestMapping(value = "/metrics/namespaces", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<ZcpNamespaceList> getNamespaces(@RequestParam(required = true, value = "userId") String userId)
			throws Exception {
		Response<ZcpNamespaceList> response = new Response<>();
		response.setData(metricService.getNamespaces(userId));

		return response;
	}

	@RequestMapping(value = "/metrics/nodes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<ZcpNodeList> getNodes() throws Exception {
		Response<ZcpNodeList> response = new Response<>();
		response.setData(metricService.getNodes());

		return response;
	}

	@RequestMapping(value = "/metrics/pods/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<PodsStatusMetricsVO> getPodsStatus(
			@RequestParam(required = false, value = "namespace") String namespace) throws Exception {
		Response<PodsStatusMetricsVO> response = new Response<>();
		response.setData(metricService.getPodsStatusMetrics(namespace));

		return response;
	}

	@RequestMapping(value = "/metrics/deployments/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<DeploymentsStatusMetricsVO> getDeploymentsStatus(
			@RequestParam(required = false, value = "namespace") String namespace) throws Exception {
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

	@RequestMapping(value = "/metrics/users/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<UsersStatusMetricsVO> getUserStatus(
			@RequestParam(required = false, value = "namespace") String namespace) throws Exception {
		Response<UsersStatusMetricsVO> response = new Response<>();
		response.setData(metricService.getUsersStatusMetrics(namespace));

		return response;
	}

	@RequestMapping(value = "/metrics/cluster/{type}/status/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<ClusterStatusMetricsVO> getMemoryStatus(@PathVariable("type") String type) throws Exception {
		Response<ClusterStatusMetricsVO> response = new Response<>();
		response.setData(metricService.getClusterMetrics(type));

		return response;
	}

}
