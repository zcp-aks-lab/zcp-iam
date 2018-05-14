package com.skcc.cloudz.zcp.member.vo;

import io.kubernetes.client.models.V1ServiceAccount;

public class ServiceAccountVO extends V1ServiceAccount{
	String namespace;

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
}
