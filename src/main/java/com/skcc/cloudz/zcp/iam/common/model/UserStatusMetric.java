package com.skcc.cloudz.zcp.iam.common.model;

public class UserStatusMetric {
	private ClusterRole role;
	private Integer count;

	public ClusterRole getRole() {
		return role;
	}

	public void setRole(ClusterRole status) {
		this.role = status;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public void increaseCount() {
		this.count++;
	}
}
