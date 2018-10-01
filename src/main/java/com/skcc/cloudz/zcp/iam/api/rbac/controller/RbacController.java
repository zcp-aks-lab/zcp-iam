package com.skcc.cloudz.zcp.iam.api.rbac.controller;

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

import com.skcc.cloudz.zcp.iam.api.rbac.service.RbacService;
import com.skcc.cloudz.zcp.iam.common.vo.Response;

import io.kubernetes.client.models.V1ClusterRoleList;
import io.kubernetes.client.models.V1RoleBinding;

@Configuration
@RestController
@RequestMapping("/iam")
public class RbacController {

	@SuppressWarnings("unused")
	private final Logger logger = LoggerFactory.getLogger(RbacController.class);

	@Autowired
	private RbacService rbacService;

	@RequestMapping(value = "/rbac/clusterRoles", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<V1ClusterRoleList> getClusterRoleList(@RequestParam(required = false, value = "type") String type)
			throws Exception {
		Response<V1ClusterRoleList> response = new Response<>();
		response.setData(rbacService.getClusterRoles(type));
		return response;
	}

	@RequestMapping(value = "/rbac/rolebinding/{namespace}/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Response<V1RoleBinding> getNamespacedRoleBinding(@PathVariable("namespace") String namespace, @PathVariable("id") String id)
			throws Exception {
		Response<V1RoleBinding> response = new Response<>();
		response.setData(rbacService.getNamespacedRoleBinding(namespace, id));
		return response;
	}
}
