package com.skcc.cloudz.zcp.iam.manager.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.skcc.cloudz.zcp.iam.manager.KubeCoreManager;
import com.skcc.cloudz.zcp.iam.manager.ResourcesNameManager;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1Secret;
import io.kubernetes.client.models.V1ServiceAccount;

@Component
public class ServiceAccountApiKeyHolder {
	private ThreadLocal<String> username = new ThreadLocal<>();
	private ThreadLocal<String> namespace = new ThreadLocal<>();
	private Map<String, String> tokens = Maps.newHashMap();
	
	@Autowired
	private KubeCoreManager manager;
	private static ServiceAccountApiKeyHolder instance;


	@Value("${zcp.kube.namespace}")
	private String defaultNamespace;
	
	public ServiceAccountApiKeyHolder() {
		if(instance != null)
			throw new RuntimeException();
		
		instance = this;
	}
	
	public static ServiceAccountApiKeyHolder instance() {
		return instance;
	}

	public String getToken() {
		String id = getUsername();
		String token = tokens.get(id);
		
		if(id == null)
			throw new RuntimeException();
		
		if(token == null)
			throw new RuntimeException();

		return token;
	}
	
	public String setToken(String username) throws ApiException {
		return setToken(defaultNamespace, username);
	}

	public String setToken(String namespace, String username) throws ApiException {
		String serviceAccountName = ResourcesNameManager.getServiceAccountName(username);
		V1ServiceAccount sa = manager.getServiceAccount(namespace, serviceAccountName);

		String secretName = sa.getSecrets().get(0).getName();
		V1Secret secret = manager.getSecret(namespace, secretName);
		
		String token = new String(secret.getData().get("token"));
		tokens.put(username, token);
		
		setUsername(username);
		setNamespace(namespace);

		return token;
	}

	/*
	 * getter & setter
	 */
	public void setUsername(String username) {
		this.username.set(username);
	}

	public String getUsername() {
		return username.get();
	}
	
	public void setNamespace(String namespace) {
		this.namespace.set(namespace);
	}
	
	public String getNamespace() {
		return namespace.get();
	}

	public void setDefaultNamespace(String defaultNamespace) {
		this.defaultNamespace = defaultNamespace;
	}

	public String getDefaultNamespace() {
		return defaultNamespace;
	}
}
