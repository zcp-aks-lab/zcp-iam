package com.skcc.cloudz.zcp.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClusterRole {
	NONE("none"), 
	VIEW("view"), 
	EDIT("edit"), 
	ADMIN("admin"), 
	CLUSTER_ADMIN("cluster-admin");

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

		return null;
	}

}
