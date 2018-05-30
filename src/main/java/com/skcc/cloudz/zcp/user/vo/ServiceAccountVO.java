package com.skcc.cloudz.zcp.user.vo;

import com.skcc.cloudz.zcp.common.vo.Ivo;

import io.kubernetes.client.models.V1ServiceAccount;

public class ServiceAccountVO extends V1ServiceAccount implements Ivo{
	String namespace;

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
}
