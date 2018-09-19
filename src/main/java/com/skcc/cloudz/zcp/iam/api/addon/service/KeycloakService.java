package com.skcc.cloudz.zcp.iam.api.addon.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.skcc.cloudz.zcp.iam.api.namespace.service.NamespaceEventListener;
import com.skcc.cloudz.zcp.iam.common.exception.KeyCloakException;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpException;
import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;
import com.skcc.cloudz.zcp.iam.common.props.RoleProperties;
import com.skcc.cloudz.zcp.iam.manager.KeyCloakManager;

@Service
public class KeycloakService implements NamespaceEventListener {
	private final Logger log = LoggerFactory.getLogger(KeycloakService.class);
	
	@Autowired
	private KeyCloakManager keyCloakManager;

	@Autowired
	private RoleProperties roleMapping;
	
	public void onCreateNamespace(String namespace) throws ZcpException {}

	public void onDeleteNamespace(String namespace) throws ZcpException {}

	public void addNamespaceRoles(String namespace, String username, ClusterRole role) throws ZcpException {
		try {
			List<String> realmRoles = roleMapping.getNamspaceUserRoles(role, namespace);
			keyCloakManager.addRealmRoles(username, realmRoles, namespace);
		} catch (KeyCloakException e) {
			log.error("", e);
			throw new ZcpException("N0100", e.getMessage());
		}
	}

	public void deleteNamspaceRoles(String namespace, String username, ClusterRole role) throws ZcpException {
		try {
			List<String> realmRoles = roleMapping.getNamspaceUserRoles(role, namespace);
			keyCloakManager.deleteRealmRoles(username, realmRoles, namespace);
		} catch (KeyCloakException e) {
			log.error("", e);
			throw new ZcpException("N0100", e.getMessage());
		}
	}
}
