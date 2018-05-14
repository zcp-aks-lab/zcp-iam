package com.skcc.cloudz.zcp.member.vo;

import io.kubernetes.client.models.V1DeleteOptions;

public class KubeDeleteOptionsVO extends V1DeleteOptions {

	String name;
	String namespace;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	
}
