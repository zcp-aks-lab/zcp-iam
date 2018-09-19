package com.skcc.cloudz.zcp.iam.api.addon.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.skcc.cloudz.zcp.iam.api.namespace.service.NamespaceEventListener;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpException;
import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;

/**
 * <code>NamespaceEventListener</code> 구현체에 대한 Composite Class.
 * Addon 전체에 Event를 전달한다. (callback 호출)
 */
@Service
public class AddonService implements NamespaceEventListener {

	@Autowired
	private List<NamespaceEventListener> lifecyleListener;
	
	public void onCreateNamespace(String namespace) throws ZcpException {
		for(NamespaceEventListener l : lifecyleListener) {
			l.onCreateNamespace(namespace);
		}
	}

	public void onDeleteNamespace(String namespace) throws ZcpException {
		for(NamespaceEventListener l : lifecyleListener) {
			l.onDeleteNamespace(namespace);
		}
	}

	public void addNamespaceRoles(String namespace, String username, ClusterRole role) throws ZcpException {
		for(NamespaceEventListener l : lifecyleListener) {
			l.addNamespaceRoles(namespace, username, role);
		}
	}

	public void deleteNamspaceRoles(String namespace, String username, ClusterRole role) throws ZcpException {
		for(NamespaceEventListener l : lifecyleListener) {
			l.deleteNamspaceRoles(namespace, username, role);
		}
	}
}
