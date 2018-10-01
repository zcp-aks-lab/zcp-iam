package com.skcc.cloudz.zcp.iam.api.rbac.service;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.skcc.cloudz.zcp.iam.common.exception.KeyCloakException;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpErrorCode;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpException;
import com.skcc.cloudz.zcp.iam.manager.KeyCloakManager;
import com.skcc.cloudz.zcp.iam.manager.KubeRbacAuthzManager;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1ClusterRoleList;
import io.kubernetes.client.models.V1RoleBinding;

@Service
public class RbacService {

	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(RbacService.class);

	@Autowired
	private KeyCloakManager keyCloakManager;
	
	@Autowired
	private KubeRbacAuthzManager kubeRbacAuthzManager;

	public V1ClusterRoleList getClusterRoles(String type) throws ZcpException {
		if (StringUtils.isEmpty(type)) {
			throw new ZcpException(ZcpErrorCode.CLUSTER_ROLE_TYPE_ERROR);
		}
		
		if (!StringUtils.equals(type, "cluster") && !StringUtils.equals(type, "namespace")) {
			throw new ZcpException(ZcpErrorCode.UNSUPPORTED_TYPE, "Unsupported type (" + type + ")");
		}
		
		try {
			return kubeRbacAuthzManager.getClusterRoles(type);
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.CLUSTER_ROLE_ERROR, e);
		}
	}

	public V1RoleBinding getNamespacedRoleBinding(String namespace, String id) throws ZcpException {
		UserRepresentation userRepresentation = null;
		try {
			userRepresentation = keyCloakManager.getUser(id);
		} catch (KeyCloakException e) {
			throw new ZcpException(ZcpErrorCode.USER_NOT_FOUND, e);
		}
		
		V1RoleBinding v1RoleBinding = null;
		try {
			v1RoleBinding = kubeRbacAuthzManager.getRoleBindingByUserName(namespace, userRepresentation.getUsername());
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.ROLEBINDING_NOT_FOUND_ERROR, e);
		}
		
		return v1RoleBinding;
	}

}
