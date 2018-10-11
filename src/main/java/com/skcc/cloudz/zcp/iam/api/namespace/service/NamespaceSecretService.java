package com.skcc.cloudz.zcp.iam.api.namespace.service;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.skcc.cloudz.zcp.iam.api.namespace.vo.SecretDockerVO;
import com.skcc.cloudz.zcp.iam.api.namespace.vo.SecretTlsVO;
import com.skcc.cloudz.zcp.iam.api.namespace.vo.SecretVO;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpErrorCode;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpException;
import com.skcc.cloudz.zcp.iam.manager.KubeCoreManager;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1Secret;
import io.kubernetes.client.models.V1SecretList;
import io.kubernetes.client.models.V1Status;

@Service
public class NamespaceSecretService {
	private String DESC = "cloudzcp.io/description";

	private final Logger log = LoggerFactory.getLogger(NamespaceSecretService.class);

	@Autowired
	private KubeCoreManager kubeCoreManager;

	private ObjectMapper mapper = new ObjectMapper();

	
	public V1SecretList getSecrets(String namespace, List<String> types) throws ZcpException {
		try {
			// kubectl get secret | grep -v account-token | grep -v Opaque | grep -v istio
			return kubeCoreManager.getSecretList(namespace, types);
		} catch (ApiException e) {
			log.error("", e);
			throw new ZcpException(ZcpErrorCode.GET_SECRET_LIST, e.getMessage());
		}
	}

	public V1Secret getSecret(String namespace, String name) throws ZcpException {
		try {
			V1Secret secret = kubeCoreManager.getSecret(namespace, name);
			
			if("kubernetes.io/dockerconfigjson".equals(secret.getType())) {
				byte[] data = secret.getData().get(".dockerconfigjson");
				JsonNode root = mapper.readTree(new String(data));
				String server = root.get("auths").fieldNames().next();
				JsonNode raw = root.at("/auths/" + server);

				secret.putStringDataItem("server", server);
				secret.putStringDataItem("username", raw.get("username").textValue());
				secret.putStringDataItem("password", raw.get("password").textValue());
				secret.putStringDataItem("email", raw.get("email").textValue());
			} else if("kubernetes.io/tls".equals(secret.getType())) {
				secret.putStringDataItem("tls.crt", "");
				secret.putStringDataItem("tls.key", "");
			}
			
			return secret;
		} catch (ApiException | IOException e) {
			log.error("", e);
			throw new ZcpException(ZcpErrorCode.GET_SECRET, e.getMessage());
		}
	}

	public byte[] getSecretData(String namespace, String name, String key) throws ZcpException {
		try {
			V1Secret secret = kubeCoreManager.getSecret(namespace, name);
			byte[] dataItem = secret.getData().get(key);
			
			if(dataItem == null)
				throw new ZcpException(ZcpErrorCode.SECRET_DATA_KEY_NOT_FOUND, "There is no data(key) in the secret.");
			
			return dataItem;
		} catch (ApiException e) {
			log.error("", e);
			throw new ZcpException(ZcpErrorCode.GET_SECRET, e.getMessage());
		}
	}

	public V1Secret createDockerSecret(String namespace, SecretDockerVO vo) throws ZcpException {
		// rebuild json
		String auth = String.format("%s:%s", vo.getUsername(), vo.getPassword());
		auth = Base64.getEncoder().encodeToString(auth.getBytes());
		
		Map<String, Object> data = Maps.newHashMap();
		data.put("username", vo.getUsername());
		data.put("password", vo.getPassword());
		data.put("email", vo.getEmail());
		data.put("auth", auth);
		
		data = ImmutableMap.of(vo.getServer(), data);
		data = ImmutableMap.of("auths", data);
		
		try {
			V1Secret secret = toSecret(vo, namespace);
			secret.putDataItem(".dockerconfigjson", toJson(data).getBytes());
			secret.getMetadata().putAnnotationsItem(DESC, vo.getDescription());

			return kubeCoreManager.createSecret(namespace, secret);
		} catch (ApiException e) {
			log.error(e.getResponseBody());
			log.error("", e);
			throw new ZcpException(ZcpErrorCode.CREATE_SECRET_ERROR, e.getMessage());
		}
	}
	
	public V1Secret createTlsSecret(String namespace, SecretTlsVO vo) throws ZcpException {
		try {
			V1Secret secret = toSecret(vo, namespace);
			secret.putDataItem("tls.crt", vo.getCrt().getBytes());
			secret.putDataItem("tls.key", vo.getKey().getBytes());

			secret.getMetadata().putAnnotationsItem(DESC, vo.getDescription());

			return kubeCoreManager.createSecret(namespace, secret);
		} catch (IOException e) {
			log.error("", e);
			throw new ZcpException(ZcpErrorCode.CREATE_SECRET_ERROR, e.getMessage());
		} catch (ApiException e) {
			log.error("", e);
			throw new ZcpException(ZcpErrorCode.CREATE_SECRET_ERROR, e.getMessage());
		}
	}

	public V1Status deleteSecret(String namespace, String name) throws ZcpException {
		try {
			return kubeCoreManager.deleteSecret(namespace, name);
		} catch (ApiException e) {
			log.error("{}", e.getMessage());
			log.debug("", e);
			log.debug("", e.getResponseBody());

			throw new ZcpException(ZcpErrorCode.DELETE_SECRET_ERROR, e.getMessage());
		}
	}
	
	private V1Secret toSecret(SecretVO vo, String namespace) {
		V1ObjectMeta metadata = new V1ObjectMeta();
		metadata.setName(vo.getName());
		metadata.setNamespace(namespace);

		V1Secret secret = new V1Secret();
		secret.setType(vo.getType());
		secret.setMetadata(metadata);
		
		return secret;
	}
	
	private String toJson(Object obj) throws ZcpException {
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			log.error("", e);
			throw new ZcpException(ZcpErrorCode.INVALID_ARGUMENT, e.getMessage());
		}
	}
}
