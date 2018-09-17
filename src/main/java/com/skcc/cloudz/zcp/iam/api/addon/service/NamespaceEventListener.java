package com.skcc.cloudz.zcp.iam.api.addon.service;

import com.skcc.cloudz.zcp.iam.common.exception.KeyCloakException;
import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;

public interface NamespaceEventListener {
	public void onCreateNamespace(String namespace);
	public void onDeleteNamespace(String namespace);
	public void addNamespaceRoles(String namespace, String username, ClusterRole clusterRole) throws KeyCloakException; 
	public void deleteNamspaceRoles(String namespace, String username, String oldRoleName) throws KeyCloakException;
}
