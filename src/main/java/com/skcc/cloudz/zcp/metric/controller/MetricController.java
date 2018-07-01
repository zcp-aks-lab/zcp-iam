package com.skcc.cloudz.zcp.metric.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.skcc.cloudz.zcp.common.model.V1alpha1NodeMetricList;
import com.skcc.cloudz.zcp.common.model.ZcpNamespaceList;
import com.skcc.cloudz.zcp.common.model.ZcpNodeList;
import com.skcc.cloudz.zcp.common.vo.Response;
import com.skcc.cloudz.zcp.metric.service.MetricService;

@Configuration
@RestController
@RequestMapping("/iam")
public class MetricController {

	private final Logger logger = LoggerFactory.getLogger(MetricController.class);

	@Autowired
	private MetricService metricService;

	@RequestMapping(value = "/metrics/nodes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<V1alpha1NodeMetricList> getUserList() throws Exception {
		Response<V1alpha1NodeMetricList> response = new Response<>();
		response.setData(metricService.getNodeMetrics());
		
		logger.debug(response.getData().getItems().toString());
		
		return response;
	}

	@RequestMapping(value = "/metrics/namespaces", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<ZcpNamespaceList> getNamespaceList() throws Exception {
		Response<ZcpNamespaceList> response = new Response<>();
		response.setData(metricService.getNamespaceList());
		
		return response;
	}

	@RequestMapping(value = "/nodes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<ZcpNodeList> getNodeList() throws Exception {
		Response<ZcpNodeList> response = new Response<>();
		response.setData(metricService.getNodeList());
		
		return response;
	}

}
