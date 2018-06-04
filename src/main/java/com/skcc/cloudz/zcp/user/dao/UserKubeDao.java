package com.skcc.cloudz.zcp.user.dao;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;
import com.skcc.cloudz.zcp.common.util.KubeClient;
import com.skcc.cloudz.zcp.common.vo.RoleBindingVO;
import com.squareup.okhttp.Call;

import ch.qos.logback.classic.Logger;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.ApiResponse;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.models.V1ClusterRoleBinding;
import io.kubernetes.client.models.V1ClusterRoleBindingList;
import io.kubernetes.client.models.V1ClusterRoleList;
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1LimitRange;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1NamespaceList;
import io.kubernetes.client.models.V1ResourceQuota;
import io.kubernetes.client.models.V1RoleBinding;
import io.kubernetes.client.models.V1RoleBindingList;
import io.kubernetes.client.models.V1Secret;
import io.kubernetes.client.models.V1ServiceAccount;
import io.kubernetes.client.models.V1ServiceAccountList;
import io.kubernetes.client.models.V1Status;
import io.kubernetes.client.proto.Meta.Status;
import io.kubernetes.client.util.Config;

@Component
public class UserKubeDao {

	private final Logger LOG = (Logger) LoggerFactory.getLogger(UserKubeDao.class);    
	   
	ApiClient client;// = Configuration.getDefaultApiClient();
	KubeClient api; // = new KubeClient(this.client);
	
	public UserKubeDao() throws IOException {
		client = Config.defaultClient();
		Configuration.setDefaultApiClient(client);
		api = new KubeClient(this.client);
	}

	
	@SuppressWarnings("unchecked")
	public V1ClusterRoleList clusterRoleList() throws ApiException{
		Call call =  api.getApiCall2(
				"/apis/rbac.authorization.k8s.io/v1/clusterroles" 
				,null, null, null, "zcp-system-user=true", null, null, null, null, null, null, null);
		Type localVarReturnType = new TypeToken<V1ClusterRoleList>(){}.getType();
		ApiResponse<V1ClusterRoleList> response =  api.getClient().execute(call, localVarReturnType);
		return (V1ClusterRoleList)response.getData();
	}
	
	@SuppressWarnings("unchecked")
	public V1ClusterRoleBindingList clusterRoleBindingList() throws ApiException{
			ApiResponse<V1ClusterRoleBindingList> data = ((ApiResponse<V1ClusterRoleBindingList>) api.getApiCall(
					"/apis/rbac.authorization.k8s.io/v1/clusterrolebindings" 
					,V1ClusterRoleBindingList.class, null, null, null, null, null, null, null, null, null, null, null));
			
			return data.getData();
	}
	

	@SuppressWarnings("unchecked")
	public V1ClusterRoleBinding createClusterRoleBinding(V1ClusterRoleBinding clusterrolebinding, String username) throws ApiException{
		Map<String, String> labels = new HashMap();
		labels.put("zcp-system-user", "true");
		labels.put("zcp-system-username", username);
		clusterrolebinding.getMetadata().setLabels(labels);
		ApiResponse<V1ClusterRoleBinding> data = (ApiResponse<V1ClusterRoleBinding>) api.postApiCall(
				"/apis/rbac.authorization.k8s.io/v1/clusterrolebindings"
				,V1ClusterRoleBinding.class,clusterrolebinding, null, null, null);
		return data.getData();
	}
	
	@SuppressWarnings("unchecked")
	public Status deleteClusterRoleBinding(String name, Object deleteOptions) throws ApiException{
		ApiResponse<Status> data = (ApiResponse<Status>) api.deleteApiCall(
				"/apis/rbac.authorization.k8s.io/v1/clusterrolebindings/"+name
				,Status.class, (V1DeleteOptions)deleteOptions, null, null, null, null, null, null);
		return data.getData();
	}
	
	@SuppressWarnings("unchecked")
	public Status editClusterRoleBinding(String name, V1ClusterRoleBinding clusterrolebinding, String username) throws ApiException{
		Map<String, String> labels = new HashMap();
		labels.put("zcp-system-user", "true");
		labels.put("zcp-system-username", username);
		clusterrolebinding.getMetadata().setLabels(labels);
		ApiResponse<Status> data = (ApiResponse<Status>) api.replaceApiCall(
				"/apis/rbac.authorization.k8s.io/v1/clusterrolebindings/"+name
				,Status.class, clusterrolebinding, null, null, null);
		return data.getData();
	}
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public V1ClusterRoleList createClusterRole(Object role) throws ApiException{
		ApiResponse<V1ClusterRoleList> data = (ApiResponse<V1ClusterRoleList>) api.postApiCall(
				"/apis/rbac.authorization.k8s.io/v1/clusterroles"
				,V1ClusterRoleList.class,role, null, null, null);
		return data.getData();
	}
	
	@SuppressWarnings("unchecked")
	public V1RoleBindingList RoleBindingListOfUser(String username) throws ApiException{
		ApiResponse<V1RoleBindingList> data = (ApiResponse<V1RoleBindingList>) api.getApiCall(
				"/apis/rbac.authorization.k8s.io/v1/rolebindings" 
				,V1RoleBindingList.class,null, null, null, "zcp-system-username="+username, null, null, null, null, null, null, null);
		return data.getData();
	}
	
	@SuppressWarnings("unchecked")
	public V1RoleBindingList RoleBindingListOfNamespace(String namespace) throws ApiException{
		ApiResponse<V1RoleBindingList> data = (ApiResponse<V1RoleBindingList>) api.getApiCall(
				"/apis/rbac.authorization.k8s.io/v1/namespaces/{namespace}/rolebindings".replace("{namespace}", namespace)
				,V1RoleBindingList.class,null, null, null, "zcp-system-user=true", null, null, null, null, null, null, null);
		return data.getData();
	}
	
	@SuppressWarnings("unchecked")
	public V1RoleBinding createRoleBinding(String namespace, RoleBindingVO rolebinding) throws ApiException{
		ApiResponse<V1RoleBinding> data = (ApiResponse<V1RoleBinding>) api.postApiCall(
				"/apis/rbac.authorization.k8s.io/v1/namespaces/{namespace}/rolebindings".replace("{namespace}", namespace)
				,V1RoleBinding.class,rolebinding, null, null, null);
		return data.getData();
	}
	
	@SuppressWarnings("unchecked")
	public V1ClusterRoleList deleteRoleBinding(String namespace, String name, V1DeleteOptions deleteOptions) throws ApiException{
		ApiResponse<V1ClusterRoleList> data = (ApiResponse<V1ClusterRoleList>) api.deleteApiCall( 
				"/apis/rbac.authorization.k8s.io/v1/namespaces/{namespace}/rolebindings/{name}".replace("{namespace}", namespace).replace("{name}", name)
				,V1ClusterRoleList.class,(Object)deleteOptions, null, null, null, null, null, null);
		return data.getData();
	}
	
	@SuppressWarnings("unchecked")
	public V1ServiceAccount createServiceAccount(String namespace, V1ServiceAccount serviceAccount) throws ApiException{
		Map<String, String> labels = new HashMap();
		labels.put("zcp-system-user", "true");
		serviceAccount.getMetadata().setLabels(labels);
		ApiResponse<V1ServiceAccount> data = (ApiResponse<V1ServiceAccount>) api.postApiCall(
				"/api/v1/namespaces/"+namespace+"/serviceaccounts"
				,V1ServiceAccount.class,serviceAccount, null, null, null);
		return data.getData();
	}
	
	@SuppressWarnings("unchecked")
	public V1ServiceAccount editServiceAccount(String name, String namespace, V1ServiceAccount serviceAccount) throws ApiException{
		Map<String, String> labels = new HashMap();
		labels.put("zcp-system-user", "true");
		serviceAccount.getMetadata().setLabels(labels);
		return api.replaceNamespacedServiceAccount(name, namespace, serviceAccount, "true");
	}
	
	@SuppressWarnings("unchecked")
	public V1Status deleteServiceAccount(String accountName, String namespace, V1DeleteOptions deleteOption) throws ApiException{
		return api.deleteNamespacedServiceAccount(accountName, namespace, deleteOption, "true", null, null, null);
	}
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public V1ServiceAccountList serviceAccountList(String namespace) throws ApiException{
		ApiResponse<V1ServiceAccountList> data = (ApiResponse<V1ServiceAccountList>) api.getApiCall(
				"/api/v1/namespaces/"+namespace+"/serviceaccounts"
				,V1ServiceAccountList.class,null, null, null, null, null, null, null, null, null, null, null);
		return data.getData();
	}
	
	@SuppressWarnings("unchecked")
	public V1ServiceAccountList getServiceAccount(String namespace, String name) throws ApiException{
//		ApiResponse<V1ServiceAccountList> data = (ApiResponse<V1ServiceAccountList>) api.getApiCall(
//				"/api/v1/namespaces/"+namespace+"/serviceaccounts/" + name
//				,null, null, null, null, null, null, null, null, null, null, null);
//		Object map = (Object)data.getData();
//		LinkedTreeMap mapData = (LinkedTreeMap)map;
//		return mapData;
		return api.listNamespacedServiceAccount(namespace, "true", null, null, null, "zcp-system-user=true", null, null, null, null);
	}
	
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public Status deleteRole(String namespace, String name, Object deleteOptions) throws ApiException{
		ApiResponse<Status> data = (ApiResponse<Status>) api.deleteApiCall(
				"/apis/rbac.authorization.k8s.io/v1/namespaces/"+ namespace + "/roles/" +name
				,Status.class, (V1DeleteOptions)deleteOptions, null, null, null, null, null, null);
		return data.getData();
	}
	
	@SuppressWarnings("unchecked")
	public V1Secret getSecret(String namespace, String secretName ) throws ApiException{
		ApiResponse<V1Secret> data = (ApiResponse<V1Secret>) api.getApiCall(
				"/api/v1/namespaces/"+namespace+"/secrets/" + secretName
				,V1Secret.class,null, null, null, null, null, null, null, null, null, null, null);
		return data.getData();
}
	
	@SuppressWarnings("unchecked")
	public V1NamespaceList namespaceList(String namespace) throws ApiException{
		ApiResponse<V1NamespaceList> data = (ApiResponse<V1NamespaceList>) api.getApiCall(
				"/api/v1/namespaces{name}".replace("{name}", namespace)
				,V1NamespaceList.class,null, null, null, "zcp-system-ns=true", null, null, null, null, null, null, null);
		return data.getData();
	}

	@SuppressWarnings("unchecked")
	public V1LimitRange createLimitRanges(String namespace, Object limitRange) throws ApiException{
		ApiResponse<V1LimitRange> data = (ApiResponse<V1LimitRange>) api.postApiCall(
				"/api/v1/namespaces/{namespace}/limitranges".replace("{namespace}", namespace)
				,V1LimitRange.class,limitRange, null, null, null);
		return data.getData();
	}
	
	
	@SuppressWarnings("unchecked")
	public V1LimitRange editLimitRanges(String namespace, String name, V1LimitRange limitRange) throws ApiException{
		return api.replaceNamespacedLimitRange(name, namespace,  limitRange, "true");
	}
	
	@SuppressWarnings("unchecked")
	public V1LimitRange getLimitRanges(String namespace) throws ApiException{
		ApiResponse<V1LimitRange> data = (ApiResponse<V1LimitRange>) api.getApiCall(
				"/api/v1/namespaces/{namespace}/limitranges".replace("{namespace}", namespace)
				,V1LimitRange.class,null, null, null, null, null, null, null, null, null, null, null);
		return data.getData();
	}
	
	
	@SuppressWarnings("unchecked")
	public V1ResourceQuota createQuota(String namespace, Object quota) throws ApiException{
		ApiResponse<V1ResourceQuota> data = (ApiResponse<V1ResourceQuota>) api.postApiCall(
				"/api/v1/namespaces/{namespace}/resourcequotas".replace("{namespace}", namespace)
				,V1ResourceQuota.class,quota, null, null, null);
		return data.getData();
	}
	
	@SuppressWarnings("unchecked")
	public V1ResourceQuota editQuota(String namespace, String name, V1ResourceQuota quota) throws ApiException{
		return api.replaceNamespacedResourceQuota(name, namespace, quota, "true");
	}
	
	@SuppressWarnings("unchecked")
	public V1ResourceQuota getQuota(String namespace) throws ApiException{
		ApiResponse<V1ResourceQuota> data = (ApiResponse<V1ResourceQuota>) api.getApiCall(
				"/api/v1/namespaces/{namespace}/resourcequotas".replace("{namespace}", namespace)
				,V1ResourceQuota.class,null, null, null, null, null, null, null, null, null, null, null);
		return data.getData();
	}
	
	@SuppressWarnings("unchecked")
	public V1Namespace createNamespace(String namespaceName, Object namespace) throws ApiException{
		ApiResponse<V1Namespace> data = (ApiResponse<V1Namespace>) api.postApiCall(
				"/api/v1/namespaces"
				,V1Namespace.class,namespace, null, null, null);
		return data.getData();
	}
	
	@SuppressWarnings("unchecked")
	public V1Namespace editNamespace(String name, Object namespace) throws ApiException{
		return api.replaceNamespace(name, (V1Namespace) namespace, "true");
	}
		
}
