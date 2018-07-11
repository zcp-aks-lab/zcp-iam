package com.skcc.cloudz.zcp.iam.api.apps.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skcc.cloudz.zcp.iam.api.apps.service.AppsService;
import com.skcc.cloudz.zcp.iam.common.vo.Response;

@Configuration
@RestController
@RequestMapping("/iam")
public class AppsController {

	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(AppsController.class);

	@Autowired
	private AppsService appsService;

	@RequestMapping(value = "/apps/deployments", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<List<String>> getDeployments(
			@RequestParam(required = false, value = "namespace") String namespace) throws Exception {
		Response<List<String>> response = new Response<>();
		response.setData(appsService.getDeployments(namespace));

		return response;
	}

}
