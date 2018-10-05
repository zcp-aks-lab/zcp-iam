package com.skcc.cloudz.zcp.iam.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClusterRole {
	NONE("none"), 
	VIEW("view"), 
	EDIT("edit"), 
	MEMBER("member"),
	ADMIN("admin"), 
	CLUSTER_ADMIN("cluster-admin"),

	@Deprecated
	DEPLOY_MANAGER("deploy-manager"),
	CICD_MANAGER("cicd-manager"),
	DEVELOPER("developer");

	private String role;

	private ClusterRole(String role) {
		this.role = role;
	}

	public String getRole() {
		return role;
	}

	@JsonValue
	public String jsonValue() {
		return this.role;
	}

	public String toString() {
		return this.role;
	}

	@JsonCreator
	public static ClusterRole getClusterRole(String role) {
		for (ClusterRole s : values()) {
			if (s.getRole().equals(role)) {
				return s;
			}
		}

		throw new IllegalArgumentException("[" +role + "] is invalid");
	}

}
