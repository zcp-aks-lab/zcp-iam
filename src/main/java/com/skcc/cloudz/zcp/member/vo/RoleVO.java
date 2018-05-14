package com.skcc.cloudz.zcp.member.vo;

import io.kubernetes.client.models.V1ClusterRole;

public class RoleVO extends V1ClusterRole{
	String namespace;

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}	
}
