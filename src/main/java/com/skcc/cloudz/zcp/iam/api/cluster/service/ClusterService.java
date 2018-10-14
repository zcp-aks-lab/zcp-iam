package com.skcc.cloudz.zcp.iam.api.cluster.service;

import static com.skcc.cloudz.zcp.iam.common.model.ClusterRole.ADMIN;
import static com.skcc.cloudz.zcp.iam.common.model.ClusterRole.CICD_MANAGER;
import static com.skcc.cloudz.zcp.iam.common.model.ClusterRole.DEVELOPER;
import static com.skcc.cloudz.zcp.iam.common.model.ClusterRole.EDIT;
import static com.skcc.cloudz.zcp.iam.common.model.ClusterRole.VIEW;
import static com.skcc.cloudz.zcp.iam.manager.ResourcesLabelManager.SYSTEM_USERNAME_LABEL_NAME;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;
import com.skcc.cloudz.zcp.iam.common.props.RoleProperties;
import com.skcc.cloudz.zcp.iam.manager.KeyCloakManager;
import com.skcc.cloudz.zcp.iam.manager.KubeRbacAuthzManager;
import com.skcc.cloudz.zcp.iam.manager.ResourcesLabelManager;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1ClusterRole;
import io.kubernetes.client.models.V1ClusterRoleBinding;
import io.kubernetes.client.models.V1ClusterRoleBindingList;
import io.kubernetes.client.models.V1ObjectMeta;

@Service
public class ClusterService {
	private static Table<String, Boolean, Map<String, String>> LABEL = HashBasedTable.create();
	private static String CS = "cluster";
	private static String NS = "namespace";

	private final Logger log = LoggerFactory.getLogger(ClusterService.class);

	static {
		Map<String, String> label = null;
		
		for(String type : Lists.newArrayList(CS, NS)) {
			for(Boolean enable : Lists.newArrayList(Boolean.TRUE, Boolean.FALSE)) {
				if(CS.equals(type))
					label = ResourcesLabelManager.getSystemClusterRoleLabels();
				if(NS.equals(type))
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
	
	@Autowired
	private KeyCloakManager keycloakManager;
	
	@Autowired
	private RoleProperties roleProperties;
	
	public Map<String, Object> verify(String cluster, final boolean dry) {
		VerifyContext.setDryRun(dry);

		try {
			// check cluster-role
			copy(EDIT, CICD_MANAGER);
			copy(VIEW, DEVELOPER);

			disable(CS, ADMIN);
			disable(NS, EDIT, VIEW);
			enable(NS, CICD_MANAGER, DEVELOPER);
			
			// change admin->member
			V1ClusterRoleBindingList crbList = kubeRbacAuthzManager.getClusterRoleBindingList();
			for(V1ClusterRoleBinding crb : crbList.getItems()) {
				ClusterRole oldRole = roleOf(crb);

				if(oldRole != ADMIN) {
					continue;
				}

				String username = usernameOf(crb);
				ClusterRole newRole = ClusterRole.MEMBER;
				crb.getRoleRef().setName(newRole.getRole());

				if(edit(crb))
					VerifyContext.print("ClusterRole was changed. [role={}->{}, username={}]", oldRole, newRole, username);
				else
					VerifyContext.print("Fail to change ClusterRole. [role={}->{}, username={}]", oldRole, newRole, username);
			}
			
			// create keycloak roles
			for(ClusterRole cr : ClusterRole.getClusterGroup()) {
				List<String> roles = roleProperties.getClusterUserRoles(cr);
				
				if(!VerifyContext.isDryRun())
					keycloakManager.createRealmRoles(roles);

				VerifyContext.print("Create keycloak roles for ClusterRole '{}'. [role={}]", cr, roles);
			}
		} catch (ApiException e) {
			log.error("", e);
			log.error("{}", e.getResponseBody());
		}
		
		return VerifyContext.getContext();
	}

	/*
	 * for ClusterRole
	 */
	private void enable(String type, ClusterRole... role) throws ApiException {
		Map<String, String> label = LABEL.get(type, true);
		for(ClusterRole r : role) {
			if(!VerifyContext.isDryRun())
				kubeRbacAuthzManager.addClusterRoleLabel(r.toString(), label);
			
			VerifyContext.print("Enable ClusterRole [{}] in {}", r, type);
		}
	}

	private void disable(String type, ClusterRole... role) throws ApiException {
		Map<String, String> label = LABEL.get(type, false);
		for(ClusterRole r : role) {
			if(!VerifyContext.isDryRun())
				kubeRbacAuthzManager.addClusterRoleLabel(r.toString(), label);
			
			VerifyContext.print("Disable ClusterRole [{}] in {}", r, type);
		}
	}
	
	public void copy(ClusterRole from, ClusterRole to) throws ApiException {
		boolean exist = false;
		try {
			exist = kubeRbacAuthzManager.getClusterRole(to.toString()) != null;
		} catch (ApiException e) {
			if(e.getCode() == HttpStatus.NOT_FOUND.value()) {
				exist = false;
			} else {
				throw e;
			}
		}
		
		if(exist) {
			VerifyContext.print("[{}] is exist. stop to copy.", to);
			return;
		}
		
		// create ClusterRole
		if(!exist && !VerifyContext.isDryRun()) {
			V1ClusterRole sourceRole = kubeRbacAuthzManager.getClusterRole(from.toString());	
			V1ObjectMeta meta = sourceRole.getMetadata();
			meta.name(to.toString());
			meta.setResourceVersion("");
			
			kubeRbacAuthzManager.createClusterRole(sourceRole);
		}
		
		VerifyContext.print("Create ClusterRole by coping '{}' to '{}'", from, to);
	}
	
	/*
	 * for ClusterRoleBinding
	 */
	public boolean is(V1ClusterRoleBinding crb, ClusterRole role) {
		return crb.getRoleRef().getName().equals(ADMIN.getRole());
	}

	public ClusterRole roleOf(V1ClusterRoleBinding crb) {
		return ClusterRole.getClusterRole(crb.getRoleRef().getName());
	}

	public String usernameOf(V1ClusterRoleBinding crb) {
		return crb.getMetadata().getLabels().get(SYSTEM_USERNAME_LABEL_NAME);
	}

	public boolean edit(V1ClusterRoleBinding crb) throws ApiException {
		if(VerifyContext.isDryRun())
			return true;

		try {
			crb.getMetadata().setResourceVersion("");

			String name = crb.getMetadata().getName();
//			kubeRbacAuthzManager.editClusterRoleBinding(name, crb);
			kubeRbacAuthzManager.deleteClusterRoleBinding(name);
			kubeRbacAuthzManager.createClusterRoleBinding(crb);
		} catch (ApiException e) {
			log.error("{}", e.getResponseBody());
			log.error("", e);
			return false;
		}
		
		return true;
	}
}
