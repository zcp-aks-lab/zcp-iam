package com.skcc.cloudz.zcp.manager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.skcc.cloudz.zcp.common.util.KubeClient;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.ApiResponse;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.RbacAuthorizationV1Api;
import io.kubernetes.client.models.V1ClusterRoleBinding;
import io.kubernetes.client.models.V1ClusterRoleBindingList;
import io.kubernetes.client.models.V1ClusterRoleList;
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1RoleBinding;
import io.kubernetes.client.models.V1RoleBindingList;
import io.kubernetes.client.models.V1Status;
import io.kubernetes.client.util.Config;

@Component
public class KubeRbacAuthzManager {

	private final Logger log = (Logger) LoggerFactory.getLogger(KubeRbacAuthzManager.class);

	private ApiClient client;

	private RbacAuthorizationV1Api api;
	
	@Value("${kube.client.api.output.pretty}")
	private String pretty;

	public KubeRbacAuthzManager() throws IOException {
		client = Config.defaultClient();
		Configuration.setDefaultApiClient(client);
		api = new RbacAuthorizationV1Api(this.client);

		log.debug("KubeRbacAuthzManager is initialized");
	}

	public V1ClusterRoleList getClusterRoleList() throws ApiException {
		return api.listClusterRole(pretty, null, null, null, ResourcesLabelManager.getSystemLabelSelector(), null, null,
				null, null);
	}

	public V1ClusterRoleBindingList getClusterRoleBindingList() throws ApiException {
		return api.listClusterRoleBinding(pretty, null, null, null, ResourcesLabelManager.getSystemUserLabelSelector(),
				null, null, null, null);
	}

	public V1ClusterRoleBindingList getClusterRoleBindingListByUsername(String username) throws ApiException {
		return api.listClusterRoleBinding(pretty, null, null, null,
				ResourcesLabelManager.getSystemUsernameLabelSelector(username), null, null, null, null);
	}

	public V1ClusterRoleBinding createClusterRoleBinding(V1ClusterRoleBinding clusterrolebinding) throws ApiException {
		return api.createClusterRoleBinding(clusterrolebinding, pretty);
	}

	public V1Status deleteClusterRoleBinding(String clusterRoleBindingName, V1DeleteOptions deleteOptions)
			throws ApiException {
		return api.deleteClusterRoleBinding(clusterRoleBindingName, deleteOptions, pretty, null, null, null);
	}

	public V1Status deleteClusterRoleBindingByUsername(String username) throws ApiException {
		return api.deleteCollectionClusterRoleBinding(pretty, null, null, null,
				ResourcesLabelManager.getSystemUsernameLabelSelector(username), null, null, null, null);
	}

	public V1ClusterRoleBinding editClusterRoleBinding(String clusterRoleBindingName,
			V1ClusterRoleBinding clusterrolebinding) throws ApiException {
		return api.replaceClusterRoleBinding(clusterRoleBindingName, clusterrolebinding, pretty);
	}

	public V1RoleBindingList getRoleBindingListAllNamespaces() throws ApiException {
		return api.listRoleBindingForAllNamespaces(null, null, null, ResourcesLabelManager.getSystemUserLabelSelector(),
				null, pretty, null, null, null);
	}

	public V1RoleBindingList getRoleBindingListByNamespace(String namespace) throws ApiException {
		return api.listNamespacedRoleBinding(namespace, pretty, null, null, null,
				ResourcesLabelManager.getSystemUserLabelSelector(), null, null, null, null);
	}

	public V1RoleBindingList getRoleBindingListByUsername(String username) throws ApiException {
		return api.listRoleBindingForAllNamespaces(null, null, null,
				ResourcesLabelManager.getSystemUsernameLabelSelector(username), null, pretty, null, null, null);
	}

	public V1RoleBinding createRoleBinding(String namespace, V1RoleBinding rolebinding) throws ApiException {
		return api.createNamespacedRoleBinding(namespace, rolebinding, pretty);
	}

	public V1Status deleteRoleBinding(String namespace, String roleBindingName, V1DeleteOptions deleteOptions)
			throws ApiException {
		return api.deleteNamespacedRoleBinding(roleBindingName, namespace, deleteOptions, pretty, null, null, null);
	}

	public V1Status deleteRoleBindingListByUsername(String namespace, String username) throws ApiException {
		return api.deleteCollectionNamespacedRoleBinding(namespace, pretty, null, null, null,
				ResourcesLabelManager.getSystemUsernameLabelSelector(username), null, null, null, null);
	}
}
