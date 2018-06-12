package com.skcc.cloudz.zcp.manager;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private final Logger logger = (Logger) LoggerFactory.getLogger(KubeCoreManager.class);

	private ApiClient client;

	private CoreV1Api api;

	@Value("${kube.system.json.pretty}")
	private String pretty;

	public KubeCoreManager() throws IOException {
		client = Config.defaultClient();
		Configuration.setDefaultApiClient(client);
		api = new CoreV1Api(this.client);

		logger.debug("KubeCoreManager is initialized");
	}

	public V1ServiceAccount createServiceAccount(String namespace, V1ServiceAccount serviceAccount)
			throws ApiException {
		return api.createNamespacedServiceAccount(namespace, serviceAccount, pretty);
	}

	public V1ServiceAccount editServiceAccount(String namespace, String serviceAccountName,
			V1ServiceAccount serviceAccount) throws ApiException {
		return api.replaceNamespacedServiceAccount(serviceAccountName, namespace, serviceAccount, pretty);
	}

	public V1Status deleteServiceAccount(String namespace, String serviceAccountName, V1DeleteOptions deleteOption)
			throws ApiException {
		return api.deleteNamespacedServiceAccount(serviceAccountName, namespace, deleteOption, pretty, null, null,
				null);
	}

	public V1Status deleteServiceAccountListByUsername(String namespace, String username) throws ApiException {
		return api.deleteCollectionNamespacedServiceAccount(namespace, pretty, null, null, null,
				ResourcesLabelManager.getSystemUsernameLabelSelector(username), null, null, null, null);
	}

	public V1ServiceAccountList getServiceAccountListByUsername(String namespace, String username) throws ApiException {
		return api.listNamespacedServiceAccount(namespace, pretty, null, null, null,
				ResourcesLabelManager.getSystemUsernameLabelSelector(username), null, null, null, null);
	}

	public V1Secret getSecret(String namespace, String secretName) throws ApiException {
		return api.readNamespacedSecret(secretName, namespace, pretty, null, null);
	}

	public V1NamespaceList getNamespaceList() throws ApiException {
		return api.listNamespace(pretty, null, null, null, ResourcesLabelManager.getSystemLabelSelector(), null, null,
				null, null);
	}

	public V1Namespace getNamespace(String namespace) throws ApiException {
		return api.readNamespace(namespace, pretty, null, null);
	}

	public V1LimitRange createLimitRange(String namespace, V1LimitRange limitRange) throws ApiException {
		return api.createNamespacedLimitRange(namespace, limitRange, pretty);
	}

	public V1LimitRange editLimitRange(String namespace, String limitRangeName, V1LimitRange limitRange)
			throws ApiException {
		return api.replaceNamespacedLimitRange(limitRangeName, namespace, limitRange, pretty);
	}

	public V1LimitRangeList getLimitRanges(String namespace) throws ApiException {
		return api.listNamespacedLimitRange(namespace, pretty, null, null, null, null, null, null, null, null);
	}

	public V1LimitRange getLimitRanges(String namespace, String limitRangeName) throws ApiException {
		return api.readNamespacedLimitRange(limitRangeName, namespace, pretty, null, null);
	}

	public V1ResourceQuota createQuota(String namespace, V1ResourceQuota quota) throws ApiException {
		return api.createNamespacedResourceQuota(namespace, quota, pretty);
	}

	public V1ResourceQuota editQuota(String namespace, String quotaName, V1ResourceQuota quota) throws ApiException {
		return api.replaceNamespacedResourceQuota(quotaName, namespace, quota, pretty);
	}

	public V1ResourceQuotaList getQuota(String namespace) throws ApiException {
		return api.listNamespacedResourceQuota(namespace, pretty, null, null, null, null, null, null, null, null);
	}

	public V1ResourceQuota getQuota(String namespace, String quotaName) throws ApiException {
		return api.readNamespacedResourceQuota(quotaName, namespace, pretty, null, null);
	}
	
	public V1ResourceQuotaList getAllQuota() throws ApiException {
		return api.listResourceQuotaForAllNamespaces(null, null, null
				, null, null, null, null, null, null);
	}
	

	public V1Namespace createNamespace(String namespaceName, V1Namespace namespace) throws ApiException {
		return api.createNamespace(namespace, pretty);
	}
	
	public V1Namespace editNamespace(String quotaName, V1Namespace namespace) throws ApiException {
		return api.replaceNamespace(quotaName, namespace, pretty);
	}

	public V1Namespace editNamespaceLabel(String namespaceName, Object namespace) throws ApiException {
		return api.patchNamespace(namespaceName, namespace, pretty);
	}


}
