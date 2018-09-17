package com.skcc.cloudz.zcp.iam.api.addon.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.skcc.cloudz.zcp.iam.common.exception.KeyCloakException;
import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;
import com.skcc.cloudz.zcp.iam.common.props.RoleProperties;
import com.skcc.cloudz.zcp.iam.manager.KeyCloakManager;

@Service
public class JenkinsService implements NamespaceEventListener {
	@Autowired
	private KeyCloakManager keyCloakManager;

	@Autowired
	private RoleProperties roleMapping;
	
	private final String JENKINS_REALM_ROLES = "${ns}-admin,${ns}-deploy-manager,${ns}-developer";

	public void onCreateNamespace(String namespace) {
		Map<String, String> vars = ImmutableMap.of("ns", namespace);
		String[] roles = StringSubstitutor.replace(JENKINS_REALM_ROLES, vars).split(",");
		keyCloakManager.createRealmRoles(roles);
		
		this.createJenkinsFolder(namespace);
	}

	public void onDeleteNamespace(String namespace) {
		Map<String, String> vars = ImmutableMap.of("ns", namespace);
		String[] roles = StringSubstitutor.replace(JENKINS_REALM_ROLES, vars).split(",");
		keyCloakManager.deleteRealmRoles(roles);
	}

	public void addNamespaceRoles(String namespace, String username, ClusterRole role) throws KeyCloakException {
		List<String> realmRoles = roleMapping.getNamspaceUserRoles(role, namespace);
		keyCloakManager.addRealmRoles(username, realmRoles, namespace);
	}

	public void deleteNamspaceRoles(String namespace, String username, String role) throws KeyCloakException {
		List<String> realmRoles = roleMapping.getNamspaceUserRoles(role, namespace);
		keyCloakManager.deleteRealmRoles(username, realmRoles, namespace);
	}
	
	public void createJenkinsFolder(String namespace) {
		//TODO
	}

}
