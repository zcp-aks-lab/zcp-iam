package com.skcc.cloudz.zcp.iam.api.resource.service;

import java.util.List;

import com.google.common.collect.Lists;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpErrorCode;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpException;
import com.skcc.cloudz.zcp.iam.manager.KubeCoreManager;
import com.skcc.cloudz.zcp.iam.manager.KubeRbacAuthzManager;
import com.skcc.cloudz.zcp.iam.manager.KubeResourceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1ListMeta;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1NamespaceList;
import io.kubernetes.client.models.V1RoleBinding;
import io.kubernetes.client.models.V1RoleBindingList;

@Service
public class ResourceService {
	private final Logger log = LoggerFactory.getLogger(ResourceService.class);

	@Autowired
	private KubeResourceManager resourceManager;

	@Autowired
	private KubeRbacAuthzManager rbacManager;

	@Autowired
	private KubeCoreManager coreManager;

	public String toKind(String shortName) throws ZcpException {
		return resourceManager.toKind(shortName);
	}

	public <T> T getList(String namespace, String kind) throws ZcpException {
		try {
			// kubectl get secret | grep -v account-token | grep -v Opaque | grep -v istio
			return resourceManager.getList(namespace, kind);
		} catch (ApiException e) {
			log.info("{}({})", e.getMessage(), e.getCode());
			log.debug("{}", e.getResponseBody());
			throw new ZcpException(ZcpErrorCode.GET_SECRET_LIST, e.getMessage());
		}
	}

	public Object getListNamespace(String username) throws ApiException {
		try {
			return resourceManager.getList("", "namespace");
		} catch (ApiException e){
			if(e.getCode() != 401)
				throw e;
		}

		List<V1Namespace> items = Lists.newArrayList();

		V1RoleBindingList rbs = rbacManager.getRoleBindingListByUsername(username);
		for(V1RoleBinding rb : rbs.getItems()){
			String namespace = rb.getMetadata().getNamespace();
			V1Namespace ns = coreManager.getNamespace(namespace);
			items.add(ns);
		}

		V1NamespaceList list = new V1NamespaceList();
		list.kind("List");
		list.metadata(new V1ListMeta());
		list.items(items);

		return list;
	}
}
