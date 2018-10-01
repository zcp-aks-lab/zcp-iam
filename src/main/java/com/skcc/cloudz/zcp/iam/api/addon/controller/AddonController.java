package com.skcc.cloudz.zcp.iam.api.addon.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skcc.cloudz.zcp.iam.api.addon.service.JenkinsService;
import com.skcc.cloudz.zcp.iam.common.vo.Response;

@Profile("default")
@RestController
@RequestMapping("/iam")
public class AddonController {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AddonController.class);

	@Autowired
	private JenkinsService jenkinsService;

	@RequestMapping(value = "/addon/jenkins/folder", method = RequestMethod.POST)
	public Response<Object> createFolder(@RequestParam("namespace") String namespace) throws Exception {
		Response<Object> response = new Response<>();
		
		jenkinsService.createJenkinsFolder(namespace);
		
		return response;
	}

}