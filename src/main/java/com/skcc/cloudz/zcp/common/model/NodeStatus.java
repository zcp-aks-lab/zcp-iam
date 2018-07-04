package com.skcc.cloudz.zcp.common.model;

public enum NodeStatus {
	Ready, NotReady, Unknown;
//	READY("Ready"), NOT_READY("NotReady"), UNKNOWN("Unknown");
//
//	private String status;
//
//	private NodeStatus(String status) {
//		this.status = status;
//	}
//
//	@JsonValue
//	public String getStatus() {
//		return status;
//	}
//
//	public void setStatus(String status) {
//		this.status = status;
//	}
//
//	@JsonCreator
//	public static NodeStatus getNodeStatus(String status) {
//		for (NodeStatus s : values()) {
//			if (s.getStatus().equals(status)) {
//				return s;
//			}
//		}
//
//		throw new IllegalArgumentException("[" + status + "] is invalid");
//	}

}
