package com.skcc.cloudz.zcp.member.vo;

import io.kubernetes.client.models.V1DeleteOptions;

public class KubeDeleteOptionsVO extends V1DeleteOptions implements Ivo{

	String name;
	String namespace;
	String userName;
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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
