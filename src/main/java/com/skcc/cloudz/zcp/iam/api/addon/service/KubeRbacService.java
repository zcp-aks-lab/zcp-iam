package com.skcc.cloudz.zcp.iam.api.addon.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.skcc.cloudz.zcp.iam.api.addon.service.AddonService.NamespaceEventAdapter;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpException;
import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;
import com.skcc.cloudz.zcp.iam.manager.KubeRbacAuthzManager;
import com.skcc.cloudz.zcp.iam.manager.ResourcesLabelManager;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1RoleBinding;

@Service
public class KubeRbacService extends NamespaceEventAdapter {
	private final Logger log = LoggerFactory.getLogger(KubeRbacService.class);
	
	@Autowired
	private KubeRbacAuthzManager kubeRbacAuthzManager;

	public void verify(String namespace, Map<String, Object> ctx) throws ZcpException {
		// Load roleBindings
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
		
		// Change clusterRole
		V1DeleteOptions options = new V1DeleteOptions();
		options.setGracePeriodSeconds(0L);
		for(V1RoleBinding rb : binding) {
			String username = rb.getMetadata().getLabels().get(ResourcesLabelManager.SYSTEM_USERNAME_LABEL_NAME);
			String oldRole = rb.getRoleRef().getName();
			String newRole = toConvertNewRole(oldRole);
			
			rb.getRoleRef().setName(newRole);

			if(oldRole.equals(newRole)) {
				log(ctx, "No changes of roleBinding. [username={0}, role={1}]", username, oldRole);
				continue;
			}

			if(!isDryRun(ctx)) {
				try {
					kubeRbacAuthzManager.deleteRoleBinding(namespace, rb.getMetadata().getName(), options);

					rb.getMetadata().resourceVersion("");
					kubeRbacAuthzManager.createRoleBinding(namespace, rb);
				} catch (ApiException e) {
					log.error("", e);
					log.error("{}", e.getCode());
					log.error("{}", e.getResponseBody());
					Object[] args = { username, oldRole, newRole, namespace };
					log(ctx, "Fail to a change roleBinding. [username={0}, role={1}->{2}, namespace={3}]", args);
					continue;
				}
			}
			
			Object[] args = { username, oldRole, newRole };
			log(ctx, "Recreate kube roleBinding. [username={0}, role={1}->{2}]", args);
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
