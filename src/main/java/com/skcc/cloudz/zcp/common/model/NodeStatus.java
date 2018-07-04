package com.skcc.cloudz.zcp.common.model;

public enum NodeStatus {
	// this is a condition's status of node's status
	Ready, NotReady, Unknown;

	public static final String STATUS_CONDITION_TYPE = "Ready";
}
