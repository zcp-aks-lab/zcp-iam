package com.skcc.cloudz.zcp.member.vo;

import io.kubernetes.client.models.V1RoleBinding;

public class RoleBindingVO extends V1RoleBinding implements Ivo{
	String namespace;
	String userName;
	String role;
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}	
}
