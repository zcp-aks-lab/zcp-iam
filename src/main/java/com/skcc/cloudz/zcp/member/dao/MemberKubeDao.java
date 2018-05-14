package com.skcc.cloudz.zcp.member.dao;

import java.io.IOException;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.internal.LinkedTreeMap;
import com.skcc.cloudz.zcp.common.util.KubeClient;

import ch.qos.logback.classic.Logger;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.ApiResponse;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.models.V1ClusterRoleBindingList;
import io.kubernetes.client.models.V1ClusterRoleList;
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1NamespaceList;
import io.kubernetes.client.models.V1RoleList;
import io.kubernetes.client.models.V1Secret;
import io.kubernetes.client.models.V1ServiceAccount;
import io.kubernetes.client.models.V1ServiceAccountList;
import io.kubernetes.client.proto.Meta.Status;
import io.kubernetes.client.util.Config;

@Component
public class MemberKubeDao {

	private final Logger LOG = (Logger) LoggerFactory.getLogger(MemberKubeDao.class);    
	   
	ApiClient client;// = Configuration.getDefaultApiClient();
	KubeClient<?> api; // = new KubeClient(this.client);
	
	public MemberKubeDao() throws IOException {
		client = Config.defaultClient();
		Configuration.setDefaultApiClient(client);
		api = new KubeClient(this.client);
	}

	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap clusterRoleList() throws ApiException{
		return (LinkedTreeMap) api.ctlData(() ->{
				ApiResponse<V1ClusterRoleList> data = (ApiResponse<V1ClusterRoleList>) api.getApiCall(
						"/apis/rbac.authorization.k8s.io/v1/clusterroles" 
						,null, null, null, null, null, null, null, null, null, null, null);
				Object map = (Object)data.getData();
				LinkedTreeMap mapData = (LinkedTreeMap)map;
				return mapData;
		});
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap clusterRoleBindingList() throws ApiException{
		return (LinkedTreeMap) api.ctlData(() ->{
			ApiResponse<V1ClusterRoleBindingList> data = (ApiResponse<V1ClusterRoleBindingList>) api.getApiCall(
					"/apis/rbac.authorization.k8s.io/v1/clusterrolebindings" 
					,null, null, null, null, null, null, null, null, null, null, null);
			Object map = (Object)data.getData();
			LinkedTreeMap mapData = (LinkedTreeMap)map;
			return mapData;
		});
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap createServiceAccount(String namespace, Object serviceAccount) throws ApiException{
		return (LinkedTreeMap) api.ctlData(() ->{
			ApiResponse<V1ServiceAccount> data = (ApiResponse<V1ServiceAccount>) api.postApiCall(
					"/api/v1/namespaces/"+namespace+"/serviceaccounts"
					,serviceAccount, null, null, null);
			Object map = (Object)data.getData();
			LinkedTreeMap mapData = (LinkedTreeMap)map;
			return mapData;
		});
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap deleteServiceAccount(String namespace, String name, Object serviceAccount) throws ApiException{
		return (LinkedTreeMap) api.ctlData(() ->{
			ApiResponse<V1ServiceAccount> data = (ApiResponse<V1ServiceAccount>) api.deleteApiCall(
					"/api/v1/namespaces/"+namespace+"/serviceaccounts"
					, serviceAccount, null, null, null, null, null, null);
			Object map = (Object)data.getData();
			LinkedTreeMap mapData = (LinkedTreeMap)map;
			return mapData;
		});
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap serviceAccountList(String namespace) throws ApiException{
		return (LinkedTreeMap) api.ctlData(() ->{
			ApiResponse<V1ServiceAccountList> data = (ApiResponse<V1ServiceAccountList>) api.getApiCall(
					"/api/v1/namespaces/"+namespace+"/serviceaccounts"
					,null, null, null, null, null, null, null, null, null, null, null);
			Object map = (Object)data.getData();
			LinkedTreeMap mapData = (LinkedTreeMap)map;
			return mapData;
		});
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap getServiceAccount(String namespace, String name) throws ApiException{
		return (LinkedTreeMap) api.ctlData(() ->{
			ApiResponse<V1ServiceAccountList> data = (ApiResponse<V1ServiceAccountList>) api.getApiCall(
					"/api/v1/namespaces/"+namespace+"/serviceaccounts/" + name
					,null, null, null, null, null, null, null, null, null, null, null);
			Object map = (Object)data.getData();
			LinkedTreeMap mapData = (LinkedTreeMap)map;
			return mapData;
		});
	}
	
	@SuppressWarnings("unchecked")
	public List<LinkedTreeMap> createClusterRoleBinding(Object jsonClusterrolebinding) throws ApiException{
		return (List<LinkedTreeMap>) api.ctlData(() ->{
			ApiResponse<V1ClusterRoleBindingList> data = (ApiResponse<V1ClusterRoleBindingList>) api.postApiCall(
					"/apis/rbac.authorization.k8s.io/v1/clusterrolebindings"
					,jsonClusterrolebinding, null, null, null);
			Object map = (Object)data.getData();
			List<LinkedTreeMap> mapData = (List<LinkedTreeMap>)map;
			return mapData;
		});
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap deleteClusterRoleBinding(String name, Object deleteOptions) throws ApiException{
		return (LinkedTreeMap) api.ctlData(() ->{
			ApiResponse<Status> data = (ApiResponse<Status>) api.deleteApiCall(
					"/apis/rbac.authorization.k8s.io/v1/clusterrolebindings/"+name
					, (V1DeleteOptions)deleteOptions, null, null, null, null, null, null);
			Object map = (Object)data.getData();
			return map;
		});
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap createClusterRole(Object role) throws ApiException{
		return (LinkedTreeMap) api.ctlData(() ->{
			ApiResponse<V1ClusterRoleList> data = (ApiResponse<V1ClusterRoleList>) api.postApiCall(
					"/apis/rbac.authorization.k8s.io/v1/clusterroles"
					,role, null, null, null);
			Object map = (Object)data.getData();
			return map;
		});
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap createRole(String namespace, Object role) throws ApiException{
		return (LinkedTreeMap) api.ctlData(() ->{
			ApiResponse<V1RoleList> data = (ApiResponse<V1RoleList>) api.postApiCall(
					"/apis/rbac.authorization.k8s.io/v1/namespaces/"+namespace+"/roles"
					,role, null, null, null);
			Object map = (Object)data.getData();
			return map;
		});
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap deleteRole(String namespace, String name, Object deleteOptions) throws ApiException{
		return (LinkedTreeMap) api.ctlData(() ->{
			ApiResponse<Status> data = (ApiResponse<Status>) api.deleteApiCall(
					"/apis/rbac.authorization.k8s.io/v1/namespaces/"+ namespace + "/roles/" +name
					, (V1DeleteOptions)deleteOptions, null, null, null, null, null, null);
			Object map = (Object)data.getData();
			return map;
		});
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap getSecret(String namespace, String secretName ) throws ApiException{
		return (LinkedTreeMap) api.ctlData(() ->{
			ApiResponse<V1Secret> data = (ApiResponse<V1Secret>) api.getApiCall(
					"/api/v1/namespaces/"+namespace+"/secrets/" + secretName
					,null, null, null, null, null, null, null, null, null, null, null);
			Object map = (Object)data.getData();
			LinkedTreeMap mapData = (LinkedTreeMap)map;
			return mapData;
		});
	}
	
	@SuppressWarnings("unchecked")
	public LinkedTreeMap namespaceList(String namespace) throws ApiException{
		return (LinkedTreeMap) api.ctlData(() ->{
			ApiResponse<V1NamespaceList> data = (ApiResponse<V1NamespaceList>) api.getApiCall(
					"/api/v1/namespaces/{name}".replace("{name}", namespace)
					,null, null, null, null, null, null, null, null, null, null, null);
			Object map = (Object)data.getData();
			LinkedTreeMap mapData = (LinkedTreeMap)map;
			return mapData;
		});
	}
	
		
}
