package com.skcc.cloudz.zcp.member.dao;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.skcc.cloudz.zcp.common.util.KubeClient;
import com.skcc.cloudz.zcp.member.vo.RoleBindingVO;
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
import io.kubernetes.client.models.V1RoleBindingList;
import io.kubernetes.client.models.V1Secret;
import io.kubernetes.client.models.V1ServiceAccount;
import io.kubernetes.client.models.V1ServiceAccountList;
import io.kubernetes.client.models.V1Status;
import io.kubernetes.client.proto.Meta.Status;
import io.kubernetes.client.util.Config;

@Component
public class MemberKubeDao {

	private final Logger LOG = (Logger) LoggerFactory.getLogger(MemberKubeDao.class);    
	   
	ApiClient client;// = Configuration.getDefaultApiClient();
	KubeClient api; // = new KubeClient(this.client);
	
	public MemberKubeDao() throws IOException {
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
	public LinkedTreeMap clusterRoleBindingList() throws ApiException{
			ApiResponse<V1ClusterRoleBindingList> data = (ApiResponse<V1ClusterRoleBindingList>) api.getApiCall(
					"/apis/rbac.authorization.k8s.io/v1/clusterrolebindings" 
					,null, null, null, null, null, null, null, null, null, null, null);
			Object map = (Object)data.getData();
			LinkedTreeMap mapData = (LinkedTreeMap)map;
			return mapData;
	}
	

	@SuppressWarnings("unchecked")
	public LinkedTreeMap createClusterRoleBinding(V1ClusterRoleBinding clusterrolebinding, String username) throws ApiException{
		Map<String, String> labels = new HashMap();
		labels.put("zcp-system-user", "true");
		labels.put("zcp-system-username", username);
		clusterrolebinding.getMetadata().setLabels(labels);
		ApiResponse<V1ClusterRoleBinding> data = (ApiResponse<V1ClusterRoleBinding>) api.postApiCall(
				"/apis/rbac.authorization.k8s.io/v1/clusterrolebindings"
				,clusterrolebinding, null, null, null);
		Object map = (Object)data.getData();
		LinkedTreeMap mapData = (LinkedTreeMap)map;
		
		return mapData;
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap deleteClusterRoleBinding(String name, Object deleteOptions) throws ApiException{
		ApiResponse<Status> data = (ApiResponse<Status>) api.deleteApiCall(
				"/apis/rbac.authorization.k8s.io/v1/clusterrolebindings/"+name
				, (V1DeleteOptions)deleteOptions, null, null, null, null, null, null);
		Object map = (Object)data.getData();
		return (LinkedTreeMap)map;
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap editClusterRoleBinding(String name, V1ClusterRoleBinding clusterrolebinding, String username) throws ApiException{
		Map<String, String> labels = new HashMap();
		labels.put("zcp-system-user", "true");
		labels.put("zcp-system-username", username);
		clusterrolebinding.getMetadata().setLabels(labels);
		ApiResponse<Status> data = (ApiResponse<Status>) api.replaceApiCall(
				"/apis/rbac.authorization.k8s.io/v1/clusterrolebindings/"+name
				, clusterrolebinding, null, null, null);
		Object map = (Object)data.getData();
		return (LinkedTreeMap)map;
	}
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public LinkedTreeMap createClusterRole(Object role) throws ApiException{
		ApiResponse<V1ClusterRoleList> data = (ApiResponse<V1ClusterRoleList>) api.postApiCall(
				"/apis/rbac.authorization.k8s.io/v1/clusterroles"
				,role, null, null, null);
		Object map = (Object)data.getData();
		return (LinkedTreeMap)map;
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap RoleBindingList(String namespace, String username) throws ApiException{
		ApiResponse<V1RoleBindingList> data = (ApiResponse<V1RoleBindingList>) api.getApiCall(
				"/apis/rbac.authorization.k8s.io/v1/namespaces/{namespace}/rolebindings".replace("{namespace}", namespace) 
				,null, null, null, "zcp-system-username="+username, null, null, null, null, null, null, null);
		Object map = (Object)data.getData();
		return (LinkedTreeMap)map;
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap createRoleBinding(String namespace, RoleBindingVO rolebinding) throws ApiException{
		ApiResponse<V1ClusterRoleList> data = (ApiResponse<V1ClusterRoleList>) api.postApiCall(
				"/apis/rbac.authorization.k8s.io/v1/namespaces/{namespace}/rolebindings".replace("{namespace}", namespace)
				,rolebinding, null, null, null);
		Object map = (Object)data.getData();
		return (LinkedTreeMap)map;
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap deleteRoleBinding(String namespace, String name, V1DeleteOptions deleteOptions) throws ApiException{
		ApiResponse<V1ClusterRoleList> data = (ApiResponse<V1ClusterRoleList>) api.deleteApiCall( 
				"/apis/rbac.authorization.k8s.io/v1/namespaces/{namespace}/rolebindings/{name}".replace("{namespace}", namespace).replace("{name}", name)
				,(Object)deleteOptions, null, null, null, null, null, null);
		Object map = (Object)data.getData();
		return (LinkedTreeMap)map;
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap createServiceAccount(String namespace, V1ServiceAccount serviceAccount) throws ApiException{
		Map<String, String> labels = new HashMap();
		labels.put("zcp-system-user", "true");
		serviceAccount.getMetadata().setLabels(labels);
		ApiResponse<V1ServiceAccount> data = (ApiResponse<V1ServiceAccount>) api.postApiCall(
				"/api/v1/namespaces/"+namespace+"/serviceaccounts"
				,serviceAccount, null, null, null);
		Object map = (Object)data.getData();
		LinkedTreeMap mapData = (LinkedTreeMap)map;
		return mapData;
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
	public LinkedTreeMap serviceAccountList(String namespace) throws ApiException{
		ApiResponse<V1ServiceAccountList> data = (ApiResponse<V1ServiceAccountList>) api.getApiCall(
				"/api/v1/namespaces/"+namespace+"/serviceaccounts"
				,null, null, null, null, null, null, null, null, null, null, null);
		Object map = (Object)data.getData();
		LinkedTreeMap mapData = (LinkedTreeMap)map;
		return mapData;
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap getServiceAccount(String namespace, String name) throws ApiException{
		ApiResponse<V1ServiceAccountList> data = (ApiResponse<V1ServiceAccountList>) api.getApiCall(
				"/api/v1/namespaces/"+namespace+"/serviceaccounts/" + name
				,null, null, null, null, null, null, null, null, null, null, null);
		Object map = (Object)data.getData();
		LinkedTreeMap mapData = (LinkedTreeMap)map;
		return mapData;
	}
	
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public LinkedTreeMap deleteRole(String namespace, String name, Object deleteOptions) throws ApiException{
		ApiResponse<Status> data = (ApiResponse<Status>) api.deleteApiCall(
				"/apis/rbac.authorization.k8s.io/v1/namespaces/"+ namespace + "/roles/" +name
				, (V1DeleteOptions)deleteOptions, null, null, null, null, null, null);
		Object map = (Object)data.getData();
		return (LinkedTreeMap)map;
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap getSecret(String namespace, String secretName ) throws ApiException{
		ApiResponse<V1Secret> data = (ApiResponse<V1Secret>) api.getApiCall(
				"/api/v1/namespaces/"+namespace+"/secrets/" + secretName
				,null, null, null, null, null, null, null, null, null, null, null);
		Object map = (Object)data.getData();
		LinkedTreeMap mapData = (LinkedTreeMap)map;
		return mapData;
}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap namespaceList(String namespace) throws ApiException{
		ApiResponse<V1NamespaceList> data = (ApiResponse<V1NamespaceList>) api.getApiCall(
				"/api/v1/namespaces/{name}".replace("{name}", namespace)
				,null, null, null, "zcp-system-ns=true", null, null, null, null, null, null, null);
		Object map = (Object)data.getData();
		LinkedTreeMap mapData = (LinkedTreeMap)map;
		return mapData;
	}

	@SuppressWarnings("unchecked")
	public LinkedTreeMap createLimitRanges(String namespace, Object limitRange) throws ApiException{
		ApiResponse<V1LimitRange> data = (ApiResponse<V1LimitRange>) api.postApiCall(
				"/api/v1/namespaces/{namespace}/limitranges".replace("{namespace}", namespace)
				,limitRange, null, null, null);
		Object map = (Object)data.getData();
		return (LinkedTreeMap)map;
	}
	
	
	@SuppressWarnings("unchecked")
	public V1LimitRange editLimitRanges(String namespace, String name, V1LimitRange limitRange) throws ApiException{
		return api.replaceNamespacedLimitRange(name, namespace,  limitRange, "true");
	}
	
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap createQuota(String namespace, Object quota) throws ApiException{
		ApiResponse<V1ResourceQuota> data = (ApiResponse<V1ResourceQuota>) api.postApiCall(
				"/api/v1/namespaces/{namespace}/resourcequotas".replace("{namespace}", namespace)
				,quota, null, null, null);
		Object map = (Object)data.getData();
		return (LinkedTreeMap)map;
	}
	
	@SuppressWarnings("unchecked")
	public V1ResourceQuota editQuota(String namespace, String name, V1ResourceQuota quota) throws ApiException{
		return api.replaceNamespacedResourceQuota(name, namespace, quota, "true");
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap createNamespace(String namespaceName, Object namespace) throws ApiException{
		ApiResponse<V1Namespace> data = (ApiResponse<V1Namespace>) api.postApiCall(
				"/api/v1/namespaces"
				,namespace, null, null, null);
		Object map = (Object)data.getData();
		return (LinkedTreeMap)map;
	}
	
	@SuppressWarnings("unchecked")
	public V1Namespace editNamespace(String name, Object namespace) throws ApiException{
		return api.replaceNamespace(name, (V1Namespace) namespace, "true");
	}
		
}
