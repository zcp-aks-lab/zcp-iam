package com.skcc.cloudz.zcp.iam.common.model;

public class NodeStatusMetric {
	private NodeStatus status;
	private Integer count;

	public NodeStatus getStatus() {
		return status;
	}

	public void setStatus(NodeStatus status) {
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
