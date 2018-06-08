package com.skcc.cloudz.zcp.manager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1LimitRange;
import io.kubernetes.client.models.V1LimitRangeList;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1NamespaceList;
import io.kubernetes.client.models.V1ResourceQuota;
import io.kubernetes.client.models.V1ResourceQuotaList;
import io.kubernetes.client.models.V1Secret;
import io.kubernetes.client.models.V1ServiceAccount;
import io.kubernetes.client.models.V1ServiceAccountList;
import io.kubernetes.client.models.V1Status;
import io.kubernetes.client.util.Config;

@Component
public class KubeCoreManager {

	//private final Logger LOG = (Logger) LoggerFactory.getLogger(KubeCoreManager.class);    
	   
	ApiClient client;// = Configuration.getDefaultApiClient();
	CoreV1Api api; // = new KubeClient(this.client);
	
	@Value("${kube.label.zcp.user}")//iam.cloudzcp.io/zcp-system-user
	String lblZcpUser;
	
	@Value("${kube.label.zcp.username}")//iam.cloudzcp.io/zcp-system-username
	String lblZcpUsername;
	
	
	public KubeCoreManager() throws IOException {
		client = Config.defaultClient();
		Configuration.setDefaultApiClient(client);
		api = new CoreV1Api(this.client);
	}

	public V1ServiceAccount createServiceAccount(String namespace, V1ServiceAccount serviceAccount) throws ApiException{
		Map<String, String> labels = new HashMap<String, String>();
		labels.put(  lblZcpUser, "true");
		serviceAccount.getMetadata().setLabels(labels);
		
		return api.createNamespacedServiceAccount(namespace, serviceAccount, "true");
	}
	
	public V1ServiceAccount editServiceAccount(String name, String namespace, V1ServiceAccount serviceAccount) throws ApiException{
		Map<String, String> labels = new HashMap<String, String>();
		labels.put( lblZcpUser, "true");
		serviceAccount.getMetadata().setLabels(labels);
		return api.replaceNamespacedServiceAccount(name, namespace, serviceAccount, "true");
	}
	
	public V1Status deleteServiceAccount(String accountName, String namespace, V1DeleteOptions deleteOption) throws ApiException{
		return api.deleteNamespacedServiceAccount(accountName, namespace, deleteOption, "true", null, null, null);
	}
	
	
	public V1ServiceAccountList getServiceAccount(String namespace, String name) throws ApiException{
		return api.listNamespacedServiceAccount(namespace, "true", null, null, null,  lblZcpUser+"=true", null, null, null, null);
	}
	
	public V1Secret getSecret(String namespace, String secretName ) throws ApiException{
		return api.readNamespacedSecret(secretName, namespace, "true", null, null);
}
	
	public V1NamespaceList namespaceList() throws ApiException{
		return api.listNamespace("true", null, null, null,  lblZcpUser+"=true", null, null, null, null);
	}
	
	public V1Namespace namespace(String namespace) throws ApiException{
		return api.readNamespace(namespace, "true", null, null);
	}

	public V1LimitRange createLimitRanges(String namespace, V1LimitRange limitRange) throws ApiException{
		return api.createNamespacedLimitRange(namespace, limitRange, "true");
	}
	
	
	public V1LimitRange editLimitRanges(String namespace, String name, V1LimitRange limitRange) throws ApiException{
		return api.replaceNamespacedLimitRange(name, namespace,  limitRange, "true");
	}
	
	public V1LimitRangeList getLimitRanges(String namespace) throws ApiException{
		return api.listNamespacedLimitRange(namespace, "true", null, null, null, null, null, null, null, null);
	}
	
	public V1LimitRange getLimitRanges(String namespace, String name) throws ApiException{
		return api.readNamespacedLimitRange(name, namespace, "true", null, null);
	}
	
	
	public V1ResourceQuota createQuota(String namespace, V1ResourceQuota quota) throws ApiException{
		return api.createNamespacedResourceQuota(namespace, quota, "true");
	}
	
	public V1ResourceQuota editQuota(String namespace, String name, V1ResourceQuota quota) throws ApiException{
		return api.replaceNamespacedResourceQuota(name, namespace, quota, "true");
	}
	
	public V1ResourceQuotaList getQuota(String namespace) throws ApiException{
		return api.listNamespacedResourceQuota(namespace, "true", null, null, null, null, null, null, null, null);
	}
	
	public V1ResourceQuota getQuota(String namespace, String name) throws ApiException{
		return api.readNamespacedResourceQuota(name, namespace, "true", null, null);
	}
	
	public V1Namespace createNamespace(String namespaceName, V1Namespace namespace) throws ApiException{
		return api.createNamespace(namespace, "true");
	}

	public V1Namespace editNamespace(String name, Object namespace) throws ApiException{
		return api.replaceNamespace(name, (V1Namespace) namespace, "true");
	}
		
}
