package com.skcc.cloudz.zcp.iam.api.resource.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.skcc.cloudz.zcp.iam.common.exception.ZcpErrorCode;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpException;
import com.skcc.cloudz.zcp.iam.manager.KubeResourceManager;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1SecretList;

@Service
public class ResourceService {
	private final Logger log = LoggerFactory.getLogger(ResourceService.class);

	@Autowired
	private KubeResourceManager kubeResourceManager;

	public V1SecretList getSecrets(String namespace, List<String> types) throws ZcpException {
		try {
			// kubectl get secret | grep -v account-token | grep -v Opaque | grep -v istio
			return kubeResourceManager.getSecretList(namespace, types);
		} catch (ApiException e) {
			log.info("{}({})", e.getMessage(), e.getCode());
			log.debug("{}", e.getResponseBody());
			throw new ZcpException(ZcpErrorCode.GET_SECRET_LIST, e.getMessage());
		}
	}
}
