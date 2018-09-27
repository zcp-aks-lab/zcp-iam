package com.skcc.cloudz.zcp.iam.api.cluster.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;
import com.skcc.cloudz.zcp.iam.manager.KubeRbacAuthzManager;
import com.skcc.cloudz.zcp.iam.manager.ResourcesLabelManager;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1ClusterRole;
import io.kubernetes.client.models.V1ObjectMeta;

@Service
public class ClusterService {
	private static final boolean ENABLE  = true;
	private static final boolean DISABLE = false;

	private static Map<String, String> CS_ROLE_ENABLE  = getRoleLabel("cluster", ENABLE);
	private static Map<String, String> CS_ROLE_DISABLE = getRoleLabel("cluster", DISABLE);
	private static Map<String, String> NS_ROLE_ENABLE  = getRoleLabel("namespace", ENABLE);
	private static Map<String, String> NS_ROLE_DISABLE = getRoleLabel("namespace", DISABLE);

	@Autowired
	private KubeRbacAuthzManager kubeRbacAuthzManager;
	
	public Map<String, Object> verify(String cluster, boolean dry) {
		Map<String, Object> data = Maps.newHashMap();

		try {
			if(!exist(ClusterRole.DEPLOY_MANAGER))
				copyTo(ClusterRole.EDIT, ClusterRole.DEPLOY_MANAGER);

			if(!exist(ClusterRole.DEVELOPER))
				copyTo(ClusterRole.VIEW, ClusterRole.DEVELOPER);

			setClusterRole(DISABLE, ClusterRole.ADMIN);
			setNamespaceRole(DISABLE, ClusterRole.EDIT);
			setNamespaceRole(DISABLE, ClusterRole.VIEW);
			setNamespaceRole(ENABLE,  ClusterRole.DEPLOY_MANAGER);
			setNamespaceRole(ENABLE,  ClusterRole.DEVELOPER);

			if(dry) {
				setClusterRole(ENABLE, ClusterRole.ADMIN);
				setNamespaceRole(ENABLE,  ClusterRole.EDIT);
				setNamespaceRole(ENABLE,  ClusterRole.VIEW);
				setNamespaceRole(DISABLE, ClusterRole.DEPLOY_MANAGER);
				setNamespaceRole(DISABLE, ClusterRole.DEVELOPER);
			}
		} catch (ApiException e) {
			e.printStackTrace();
		}
		
		return data;
	}

	private boolean exist(ClusterRole role) throws ApiException {
		try {
			kubeRbacAuthzManager.getClusterRole(role.toString());
			return true;
		} catch (ApiException e) {
			if(e.getCode() == HttpStatus.NOT_FOUND.value())
				return false;

			throw e;
		}	
	}
	
	private void copyTo(ClusterRole source, ClusterRole target) throws ApiException {
		V1ClusterRole sourceRole = kubeRbacAuthzManager.getClusterRole(source.toString());	
		V1ObjectMeta meta = sourceRole.getMetadata();
		meta.name(target.toString());
		meta.setResourceVersion("");
		
		kubeRbacAuthzManager.createClusterRole(sourceRole);
	}
	
	private V1ClusterRole setClusterRole(boolean enable, ClusterRole role) throws ApiException {
		Map<String, String> label = enable ? CS_ROLE_ENABLE : CS_ROLE_DISABLE; 
		return kubeRbacAuthzManager.addClusterRoleLabel(role.toString(), label);
	}

	private V1ClusterRole setNamespaceRole(boolean enable, ClusterRole role) throws ApiException {
		Map<String, String> label = enable ? NS_ROLE_ENABLE : NS_ROLE_DISABLE; 
		return kubeRbacAuthzManager.addClusterRoleLabel(role.toString(), label);
	}

	private static Map<String, String> getRoleLabel(String type, boolean enable){
		Map<String, String> label = null;
		if("cluster".equals(type))
			label = ResourcesLabelManager.getSystemClusterRoleLabels();
		else if("namespace".equals(type))
			label = ResourcesLabelManager.getSystemNamespaceRoleLabels();
		
		if(!enable)
			label.replaceAll((k, v) -> "");
		
		label.putAll(ResourcesLabelManager.getSystemLabels());
		
		return label;
	}
}
