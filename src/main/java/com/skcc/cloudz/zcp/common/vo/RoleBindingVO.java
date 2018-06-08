package com.skcc.cloudz.zcp.common.vo;

import io.kubernetes.client.models.V1RoleBinding;

public class RoleBindingVO extends V1RoleBinding implements Ivo{
	String namespace;
	String userName;
	int clusterRole;
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	public int getClusterRole() {
		return clusterRole;
	}

	public void setClusterRole(int clusterRole) {
		this.clusterRole = clusterRole;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}	
}
