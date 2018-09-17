package com.skcc.cloudz.zcp.iam.api.addon.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.skcc.cloudz.zcp.iam.common.exception.KeyCloakException;
import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;

@Service
public class AddonService implements NamespaceEventListener {

	@Autowired
	private List<NamespaceEventListener> lifecyleListener;
	
	public void onCreateNamespace(String namespace) {
		lifecyleListener.forEach(l -> l.onCreateNamespace(namespace));
	}

	public void onDeleteNamespace(String namespace) {
		lifecyleListener.forEach(l -> l.onDeleteNamespace(namespace));
	}

	public void addNamespaceRoles(String namespace, String username, ClusterRole role) throws KeyCloakException {
		for(NamespaceEventListener l : lifecyleListener) {
			l.addNamespaceRoles(namespace, username, role);
		}
	}

	public void deleteNamspaceRoles(String namespace, String username, String role) throws KeyCloakException {
		for(NamespaceEventListener l : lifecyleListener) {
			l.deleteNamspaceRoles(namespace, username, role);
		}
	}
}
