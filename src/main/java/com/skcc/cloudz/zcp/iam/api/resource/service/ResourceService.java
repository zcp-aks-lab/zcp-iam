package com.skcc.cloudz.zcp.iam.api.resource.service;

import java.util.List;
import java.util.Map;

import com.skcc.cloudz.zcp.iam.api.user.service.UserService;
import com.skcc.cloudz.zcp.iam.common.actuator.SystemEndpoint.EndpointSource;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpErrorCode;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpException;
import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;
import com.skcc.cloudz.zcp.iam.manager.KubeRbacAuthzManager;
import com.skcc.cloudz.zcp.iam.manager.KubeResourceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1ClusterRoleBinding;
import io.kubernetes.client.models.V1NamespaceList;

@Service
public class ResourceService implements EndpointSource<Object> {
	private final Logger log = LoggerFactory.getLogger(ResourceService.class);

	@Autowired
	private KubeResourceManager resourceManager;

	@Autowired
	private KubeRbacAuthzManager rbacManager;

	@Autowired
	private UserService userService;

	public String toKind(String alias) {
		return resourceManager.toKind(alias);
	}

	public <T> T getList(String namespace, String kind) throws ZcpException {
		try {
			// kubectl get secret | grep -v account-token | grep -v Opaque | grep -v istio
			// kind = resourceManager.toKind(kind);
			return resourceManager.getList(namespace, kind);
		} catch (Exception e) {
			throw handleException(e);
		}
	}

	public <T> T getResource(String namespace, String kind, String name, String type) throws ZcpException {
		try {
			// kubectl get secret | grep -v account-token | grep -v Opaque | grep -v istio
			// kind = resourceManager.toKind(kind);
			return resourceManager.getResource(namespace, kind, name, type);
		} catch (Exception e) {
			throw handleException(e);
		}
	}

	public <T> T updateResource(String namespace, String kind, String name, String json) throws ZcpException {
		try {
			// kubectl get secret | grep -v account-token | grep -v Opaque | grep -v istio
			// kind = resourceManager.toKind(kind);
			return resourceManager.updateResource(namespace, kind, name, json);
		} catch (Exception e) {
			throw handleException(e);
		}
	}

	/*
	 * For custom action
	 */
	public V1NamespaceList getListNamespace(String username) throws Exception {
		try {
			V1ClusterRoleBinding crb = rbacManager.getClusterRoleBindingByUsername(username);
			ClusterRole role = ClusterRole.getClusterRole(crb.getRoleRef().getName());

			List<String> names = userService.getNamespace(username, role);
			V1NamespaceList list = resourceManager.getList("", "namespace");
			list.getItems().removeIf(v -> {
				return !names.contains(v.getMetadata().getName());
			});

			return list;
		} catch (Exception e){
			throw handleException(e);
		}
	}

	public <T> T getLogs(Map<String, Object> params) throws ZcpException {
		try {
			return resourceManager.readLogs(params);
		} catch (Exception e) {
			throw handleException(e);
		}
	}

	public ZcpException handleException(Exception e) throws ZcpException {
		ApiException ae = null;
		if (e instanceof ApiException) { ae = (ApiException) e; }
		if (e.getCause() instanceof ApiException) { ae = (ApiException) e.getCause(); }

		if (ae != null){
			log.info("{}({})", ae.getMessage(), ae.getCode());
			log.debug("{}", ae.getResponseBody());

			ZcpErrorCode zcpCode = ZcpErrorCode.KUBERNETES_UNKNOWN_ERROR; 
			switch(ae.getCode()){
				case 404: zcpCode = ZcpErrorCode.RESOURCE_NOT_FOUND; break;
				case 422: zcpCode = ZcpErrorCode.RESOURCE_INVALID; break;
			}
			throw new ZcpException(zcpCode, resourceManager.handleExceptionMessage(ae));
		}

		log.info("{}({})", e.getMessage(), e.getClass());
		throw new ZcpException(ZcpErrorCode.GET_SECRET, e.getMessage());
	}

	public String handleExceptionMessage(ApiException ae){
		try {

		} catch (Exception e) {
			log.info("{}({})", e.getMessage(), e.getClass());
		}
		return ae.getMessage();
	}

    /* for actuator (/system) */
	@Override
	public String getEndpointPath() {
		return "/k8s/supports";
	}

	@Override
	public Object getEndpointData(Map<String, Object> vars) {
		return resourceManager.getMapping();
	}
}
