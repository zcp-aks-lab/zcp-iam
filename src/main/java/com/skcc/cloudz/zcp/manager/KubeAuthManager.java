package com.skcc.cloudz.zcp.manager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.skcc.cloudz.zcp.common.vo.RoleBindingVO;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
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
public class KubeAuthManager {

	//private final Logger LOG = (Logger) LoggerFactory.getLogger(KubeAuthManager.class);    
	   
	ApiClient client;// = Configuration.getDefaultApiClient();
	RbacAuthorizationV1Api api; // = new KubeClient(this.client);
	
	String labelZcp = "zcp-system-user=true";
	
	public KubeAuthManager() throws IOException {
		client = Config.defaultClient();
		Configuration.setDefaultApiClient(client);
		api = new RbacAuthorizationV1Api(this.client);
	}

	
	public V1ClusterRoleList clusterRoleList() throws ApiException{
		return api.listClusterRole("true", null, null, null, labelZcp, null, null, null, null);
	}
	
	public V1ClusterRoleBindingList clusterRoleBindingList() throws ApiException{
		return api.listClusterRoleBinding("true", null, null, null, null, null, null, null, null);
	}
	

	public V1ClusterRoleBinding createClusterRoleBinding(V1ClusterRoleBinding clusterrolebinding, String username) throws ApiException{
		Map<String, String> labels = new HashMap<String, String>();
		labels.put("zcp-system-user", "true");
		labels.put("zcp-system-username", username);
		clusterrolebinding.getMetadata().setLabels(labels);
		return api.createClusterRoleBinding(clusterrolebinding, "true");
	}
	
	public V1Status deleteClusterRoleBinding(String name, V1DeleteOptions deleteOptions) throws ApiException{
		return api.deleteClusterRoleBinding(name, deleteOptions, "true", null, null, null);
	}
	
	public V1ClusterRoleBinding editClusterRoleBinding(String name, V1ClusterRoleBinding clusterrolebinding, String username) throws ApiException{
		Map<String, String> labels = new HashMap<String, String>();
		labels.put("zcp-system-user", "true");
		labels.put("zcp-system-username", username);
		
		return api.replaceClusterRoleBinding(name, clusterrolebinding, "true");
	}
	
	
	public V1RoleBindingList RoleBindingListOfUser(String username) throws ApiException{
		return api.listRoleBindingForAllNamespaces(null, null, null, "zcp-system-username="+username, null, "true", null, null, null);
	}
	
	public V1RoleBindingList RoleBindingListOfNamespace(String namespace) throws ApiException{
		return api.listNamespacedRoleBinding(namespace, "true", null, null, null, labelZcp, null, null, null, null);
	}
	
	public V1RoleBinding createRoleBinding(String namespace, RoleBindingVO rolebinding) throws ApiException{
		return api.createNamespacedRoleBinding(namespace, rolebinding, "true");
	}
	
	public V1Status deleteRoleBinding(String namespace, String name, V1DeleteOptions deleteOptions) throws ApiException{
		return api.deleteNamespacedRoleBinding(name, namespace, deleteOptions, "true", null, null, null);
	}
	
		
}
