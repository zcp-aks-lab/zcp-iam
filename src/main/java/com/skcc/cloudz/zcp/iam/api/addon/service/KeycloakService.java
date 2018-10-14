package com.skcc.cloudz.zcp.iam.api.addon.service;

import java.util.List;
import java.util.Map;

import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.skcc.cloudz.zcp.iam.api.addon.service.AddonService.NamespaceEventAdapter;
import com.skcc.cloudz.zcp.iam.common.exception.KeyCloakException;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpErrorCode;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpException;
import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;
import com.skcc.cloudz.zcp.iam.common.props.RoleProperties;
import com.skcc.cloudz.zcp.iam.manager.KeyCloakManager;
import com.skcc.cloudz.zcp.iam.manager.KubeRbacAuthzManager;
import com.skcc.cloudz.zcp.iam.manager.ResourcesLabelManager;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1RoleBinding;

@Service
public class KeycloakService extends NamespaceEventAdapter {
	private final Logger log = LoggerFactory.getLogger(KeycloakService.class);
	private final String CLUSTER = "cluster";
	
	@Autowired
	private KeyCloakManager keyCloakManager;
	
	@Autowired
	private KubeRbacAuthzManager kubeRbacAuthzManager;

	@Autowired
	private RoleProperties roleMapping;
	
	public void addClusterRoles(String username, ClusterRole role) throws ZcpException {
		try {
			List<String> realmRoles = roleMapping.getClusterUserRoles(role);
			keyCloakManager.addRealmRoles(username, realmRoles, CLUSTER);
		} catch (KeyCloakException e) {
			log.error("", e);
			throw new ZcpException(ZcpErrorCode.ADD_USER_CLUSTER_ROLE, e.getMessage());
		}
	}

	public void deleteClusterRoles(String username, ClusterRole role) throws ZcpException {
		try {
			List<String> realmRoles = roleMapping.getClusterUserRoles(role);
			keyCloakManager.deleteRealmRoles(username, realmRoles, CLUSTER);
		} catch (KeyCloakException e) {
			log.error("", e);
			throw new ZcpException(ZcpErrorCode.DELETE_USER_CLUSTER_ROLE, e.getMessage());
		}
	}
	
	public void addNamespaceRoles(String namespace, String username, ClusterRole role) throws ZcpException {
		try {
			List<String> realmRoles = roleMapping.getNamspaceUserRoles(role, namespace);
			keyCloakManager.addRealmRoles(username, realmRoles, namespace);
		} catch (KeyCloakException e) {
			log.error("", e);
			throw new ZcpException(ZcpErrorCode.ADD_USER_NAMESPACE_ROLE, e.getMessage());
		}
	}

	public void deleteNamspaceRoles(String namespace, String username, ClusterRole role) throws ZcpException {
		try {
			List<String> realmRoles = roleMapping.getNamspaceUserRoles(role, namespace);
			keyCloakManager.deleteRealmRoles(username, realmRoles, namespace);
		} catch (KeyCloakException e) {
			log.error("", e);
			throw new ZcpException(ZcpErrorCode.DELETE_USER_NAMESPCE_ROLE, e.getMessage());
		}
	}
	
	public void verify(String namespace, Map<String, Object> ctx) throws ZcpException {
		List<V1RoleBinding> binding = null;
		try {
			binding = kubeRbacAuthzManager.getRoleBindingListByNamespace(namespace).getItems();
			List<String> names = Lists.transform(binding, rb -> rb.getMetadata().getName());
			log(ctx, "Load roleBindings. [namespace={0}, rb={1}]", namespace, names);
		} catch (ApiException e) {
			log.error("", e);
			log(ctx, "Fail to load roleBinding. [namespace={0}]", namespace);
			return;
		}

		for(V1RoleBinding rb : binding) {
			// get namespace user role
			String username = rb.getMetadata().getLabels().get(ResourcesLabelManager.SYSTEM_USERNAME_LABEL_NAME);
			String oldRole = rb.getRoleRef().getName();
			String newRole = toConvertNewRole(oldRole);
			List<String> realmRoles = roleMapping.getNamspaceUserRoles(newRole, namespace);

			try {
				// check
				UserRepresentation user = keyCloakManager.getUserFromName(username).toRepresentation();
				if(user == null) {
					log(ctx, "No matched user in keycloak. [username={0}, ns={1}]", username, namespace);
					continue;
				}
	
				final String key = "role-" + namespace;
				Map<String, List<String>> attr = user.getAttributes();
				if(attr != null && realmRoles.equals(attr.get(key))) {
					String msg = "No changes of realm roles. [username={0}, role={1}->{2}, realm-roles={3}]";
					log(ctx, msg, username, oldRole, newRole, attr.get(key));
					continue;
				}
				
				if(!isDryRun(ctx)) {
					keyCloakManager.addRealmRoles(username, realmRoles, namespace);
				}
				
				Object[] args = { username, oldRole, newRole, realmRoles };
				log(ctx, "Change realm roles. [username={0}, role={1}->{2}, realm-roles={3}]", args);
			} catch (KeyCloakException e) {
				log.error("", e);
				Object[] args = { e.getMessage(), username, oldRole, newRole, realmRoles };
				log(ctx, "Error during verify realm roles. {0} [username={1}, role={2}->{3}, realm-roles={4}]", args);
			}
		}
	}

	private String toConvertNewRole(String old) {
		switch(ClusterRole.getClusterRole(old)) {
		case EDIT:
		case DEPLOY_MANAGER:
			return ClusterRole.CICD_MANAGER.getRole();
		case VIEW:
			return ClusterRole.DEVELOPER.getRole();
		default:
			return old;
		}
	}
}
