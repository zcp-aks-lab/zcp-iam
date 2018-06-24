package com.skcc.cloudz.zcp.namespace.vo;

import com.skcc.cloudz.zcp.common.vo.Ivo;

import io.kubernetes.client.models.V1DeleteOptions;

public class KubeDeleteOptionsVO extends V1DeleteOptions implements Ivo {

	private String name;
	private String namespace;
	private String username;

	public String getUsername() {
		return username;
	}

	public void setUsername(String userName) {
		this.username = userName;
	}

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
