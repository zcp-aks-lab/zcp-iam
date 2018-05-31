package com.skcc.cloudz.zcp.user.vo;

public enum ClusterRole {
	CLUSTER_ADMIN("CLUSTER_ADMIN"),
	ADMIN("ADMIN"),
	EDIT("EDIT"),
	VIEW("VIEW"),
	NONE("NONE");
	
	String role;
	
	private ClusterRole(String role) {
		this.role = role;
	}
	
}
