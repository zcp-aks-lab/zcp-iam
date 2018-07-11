package com.skcc.cloudz.zcp.iam.common.model;

public class DeploymentStatusMetric {
	private DeploymentStatus status;
	private Integer count;

	public DeploymentStatus getStatus() {
		return status;
	}

	public void setStatus(DeploymentStatus status) {
		this.status = status;
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
