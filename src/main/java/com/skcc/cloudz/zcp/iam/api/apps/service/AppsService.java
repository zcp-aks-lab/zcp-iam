package com.skcc.cloudz.zcp.iam.api.apps.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.skcc.cloudz.zcp.iam.common.exception.ZcpErrorCode;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpException;
import com.skcc.cloudz.zcp.iam.manager.KubeAppsManager;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1beta2Deployment;
import io.kubernetes.client.models.V1beta2DeploymentList;

@Service
public class AppsService {

	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(AppsService.class);

	@Autowired
	private KubeAppsManager kubeAppsManager;

	// TODO shoud replace return type from List<String> to V1beta2DeploymentList
	// The IllegalArgumentException is thrown during json binding
	public List<String> getDeployments(String namespace) throws ZcpException {
		V1beta2DeploymentList v1beta2DeploymentList = null;
		try {
			v1beta2DeploymentList = kubeAppsManager.getDeploymentList(namespace);
		} catch (ApiException e) {
			throw new ZcpException(ZcpErrorCode.DEPOLYMENT_LIST_ERROR, e);
		}
		
		List<String> deployments = new ArrayList<>();
		for (V1beta2Deployment v1beta2Deployment : v1beta2DeploymentList.getItems()) {
			deployments.add(v1beta2Deployment.getMetadata().getName());
		}

		return deployments;
	}

}
