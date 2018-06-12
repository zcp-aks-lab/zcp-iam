package com.skcc.cloudz.zcp.namespace.vo;

import com.skcc.cloudz.zcp.common.model.ClusterRole;
import com.skcc.cloudz.zcp.common.vo.Ivo;

import io.kubernetes.client.models.V1RoleBinding;

public class RoleBindingVO extends V1RoleBinding implements Ivo{
	String namespace;
	String userName;
	ClusterRole clusterRole;
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public ClusterRole getClusterRole() {
		return clusterRole;
	}

	public void setClusterRole(ClusterRole clusterRole) {
		this.clusterRole = clusterRole;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}	
}
