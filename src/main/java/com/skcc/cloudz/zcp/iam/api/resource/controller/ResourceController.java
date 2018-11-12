package com.skcc.cloudz.zcp.iam.api.resource.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.skcc.cloudz.zcp.iam.api.resource.service.ResourceService;
import com.skcc.cloudz.zcp.iam.common.vo.Response;
import com.skcc.cloudz.zcp.iam.manager.client.ServiceAccountApiKeyHolder;

import io.kubernetes.client.models.V1SecretList;

@RestController
@RequestMapping("/iam")
public class ResourceController {
	private final Logger logger = LoggerFactory.getLogger(ResourceController.class);

	@Autowired
	private ResourceService resourceService;
	
	@Value("${zcp.kube.namespace}")
	private String zcpSystemNamespace;

	@RequestMapping(value = "rbac/{username}/namespace/{namespace}/secrets", method = RequestMethod.GET)
	public Response<V1SecretList> getSecrets(@PathVariable("namespace") String namespace, @PathVariable("username") String username) throws Exception {
		Response<V1SecretList> response = new Response<>();
		
		ServiceAccountApiKeyHolder.instance().setToken(zcpSystemNamespace, username);
		
		response.setData(resourceService.getSecrets(namespace, null));
		return response;
	}
}
