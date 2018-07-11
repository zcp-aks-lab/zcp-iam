package com.skcc.cloudz.zcp.iam.common.model;

public class PodStatusMetric {
	private PodStatus status;
	private Integer count;

	public PodStatus getStatus() {
		return status;
	}

	public void setStatus(PodStatus status) {
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
