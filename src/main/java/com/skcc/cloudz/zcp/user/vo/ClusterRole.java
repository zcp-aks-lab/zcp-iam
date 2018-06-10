package com.skcc.cloudz.zcp.user.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClusterRole {
	NONE("none", 0), VIEW("view", 1), EDIT("edit", 2), ADMIN("admin", 3), CLUSTER_ADMIN("cluster-admin", 4);

	private String role;
	private int value;

	class SimpleRole {
		private String name;
		private int value;

		public SimpleRole(String name, int value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return this.name;
		}

		public int getValue() {
			return this.value;
		}
	}

	private ClusterRole(String role, int value) {
		this.role = role;
		this.value = value;
	}

	public String getRole() {
		return role;
	}

	public int getValue() {
		return value;
	}

	@JsonValue
	public SimpleRole jsonValue() {
		return new SimpleRole(this.role, this.value);
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

	public static ClusterRole getClusterRole(int value) {
		for (ClusterRole s : values()) {
			if (s.getValue() == value) {
				return s;
			}
		}

		return null;
	}
}
