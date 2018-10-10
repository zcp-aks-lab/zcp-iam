package com.skcc.cloudz.zcp.iam.api.namespace.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.skcc.cloudz.zcp.iam.api.namespace.service.NamespaceSecretService;
import com.skcc.cloudz.zcp.iam.api.namespace.vo.SecretDockerVO;
import com.skcc.cloudz.zcp.iam.api.namespace.vo.SecretTlsVO;
import com.skcc.cloudz.zcp.iam.common.vo.Response;

import io.kubernetes.client.models.V1Secret;
import io.kubernetes.client.models.V1SecretList;
import io.kubernetes.client.models.V1Status;

@RestController
@RequestMapping("/iam")
public class NamespaceSecretController {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(NamespaceSecretController.class);

	@Autowired
	private NamespaceSecretService secretService;

	@RequestMapping(value = "/namespace/{namespace}/secrets", method = RequestMethod.GET)
	public Response<V1SecretList> getSecrets(@PathVariable("namespace") String namespace) throws Exception {
		Response<V1SecretList> response = new Response<>();

		List<String> types = Lists.newArrayList("kubernetes.io/dockerconfigjson", "kubernetes.io/tls");
		response.setData(secretService.getSecrets(namespace, types));

		return response;
	}

	@RequestMapping(value = "/namespace/{namespace}/secret/{secret:.+}", method = RequestMethod.GET)
	public Response<Object> getSecret(
			@PathVariable("namespace") String namespace,
			@PathVariable("secret") String name) throws Exception {
		Response<Object> response = new Response<>();

		V1Secret secret = secretService.getSecret(namespace, name);
		if("kubernetes.io/tls".equals(secret.getType())) {
			secret.getStringData()
				.replaceAll( (k,v) -> String.format("/iam/namespace/%s/secret/%s/data/%s", namespace, name, k) );
		}

		response.setData(secret);
		return response;
	}
	
	@RequestMapping(value = "/namespace/{namespace}/secret/new/docker", method = RequestMethod.POST)
	public Response<V1Secret> createDockerSecret(@PathVariable("namespace") String namespace, @RequestBody SecretDockerVO vo) throws Exception {
		Response<V1Secret> response = new Response<>();
		response.setData(secretService.createDockerSecret(namespace, vo));

		return response;
	}

	@RequestMapping(value = "/namespace/{namespace}/secret/new/tls", method = RequestMethod.POST, consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public Response<V1Secret> createTlsSecret(@PathVariable("namespace") String namespace, @ModelAttribute SecretTlsVO vo) throws Exception {
		Response<V1Secret> response = new Response<>();
		response.setData(secretService.createTlsSecret(namespace, vo));

		return response;
	}

	@RequestMapping(value = "/namespace/{namespace}/secret/{secret:.+}/data/{key:.+}", method = RequestMethod.GET)
	public ResponseEntity<Resource> getTlsSecretData(@PathVariable("namespace") String namespace,
			@PathVariable("secret") String secret,
			@PathVariable("key") String key) throws Exception {

		// https://stackoverflow.com/a/16333149
		byte[] stream = secretService.getSecretData(namespace, secret, key);
		ByteArrayResource resource = new ByteArrayResource(stream);

		return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
                .body(resource);
	}

	@RequestMapping(value = "/namespace/{namespace}/secret/{secret:.+}", method = RequestMethod.DELETE)
	public Response<Object> deleteSecret(@PathVariable("namespace") String namespace, @PathVariable("secret") String name) throws Exception {
		Response<Object> response = new Response<>();

		V1Status status = secretService.deleteSecret(namespace, name);

		response.setData(status);
		return response;
	}
}
