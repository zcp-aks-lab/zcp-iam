package com.skcc.cloudz.zcp.iam.api.cluster.service;

import static com.skcc.cloudz.zcp.iam.common.model.ClusterRole.ADMIN;
import static com.skcc.cloudz.zcp.iam.common.model.ClusterRole.DEPLOY_MANAGER;
import static com.skcc.cloudz.zcp.iam.common.model.ClusterRole.DEVELOPER;
import static com.skcc.cloudz.zcp.iam.common.model.ClusterRole.EDIT;
import static com.skcc.cloudz.zcp.iam.common.model.ClusterRole.VIEW;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;
import com.skcc.cloudz.zcp.iam.manager.KubeRbacAuthzManager;
import com.skcc.cloudz.zcp.iam.manager.ResourcesLabelManager;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1ClusterRole;
import io.kubernetes.client.models.V1ObjectMeta;

@Service
public class ClusterService {
	private static Table<String, Boolean, Map<String, String>> LABEL = HashBasedTable.create();

	private final Logger log = LoggerFactory.getLogger(ClusterService.class);

	static {
		Map<String, String> label = null;
		
		for(String type : Lists.newArrayList("cs", "ns")) {
			for(Boolean enable : Lists.newArrayList(Boolean.TRUE, Boolean.FALSE)) {
				if("cs".equals(type))
					label = ResourcesLabelManager.getSystemClusterRoleLabels();
				if("ns".equals(type))
					label = ResourcesLabelManager.getSystemNamespaceRoleLabels();
				
				if(!enable)
					label.replaceAll((k, v) -> "");
				
				label.putAll(ResourcesLabelManager.getSystemLabels());
				
				LABEL.put(type, enable, label);
			}
		}
	}

	@Autowired
	private KubeRbacAuthzManager kubeRbacAuthzManager;
	
	public Map<String, Object> verify(String cluster, boolean dry) {
		Map<String, Object> data = Maps.newHashMap();

		try {
			copy(EDIT, DEPLOY_MANAGER);
			copy(VIEW, DEVELOPER);

			disable("cs", ADMIN);
			disable("ns", EDIT, VIEW);
			enable("ns", DEPLOY_MANAGER, DEVELOPER);

			if(dry) {
				enable("cs", ADMIN);
				enable("ns", EDIT, VIEW);
				disable("ns", DEPLOY_MANAGER, DEVELOPER);
			}
		} catch (ApiException e) {
			log.error("", e);
			data.put("code", e.getCode());
			data.put("msg", e.getMessage());
		}
		
		return data;
	}

	private void enable(String type, ClusterRole... role) throws ApiException {
		Map<String, String> label = LABEL.get(type, true);
		for(ClusterRole r : role)
			kubeRbacAuthzManager.addClusterRoleLabel(r.toString(), label);
	}

	private void disable(String type, ClusterRole... role) throws ApiException {
		Map<String, String> label = LABEL.get(type, false);
		for(ClusterRole r : role)
			kubeRbacAuthzManager.addClusterRoleLabel(r.toString(), label);
	}
	
	public void copy(ClusterRole from, ClusterRole to) throws ApiException {
		boolean exist = false;
		try {
			exist = kubeRbacAuthzManager.getClusterRole(to.toString()) != null;
		} catch (ApiException e) {
			if(e.getCode() == HttpStatus.NOT_FOUND.value())
				exist = false;
			throw e;
		}
		
		if(!exist) {
			V1ClusterRole sourceRole = kubeRbacAuthzManager.getClusterRole(from.toString());	
			V1ObjectMeta meta = sourceRole.getMetadata();
			meta.name(to.toString());
			meta.setResourceVersion("");
			
			kubeRbacAuthzManager.createClusterRole(sourceRole);
		}
	}
}
