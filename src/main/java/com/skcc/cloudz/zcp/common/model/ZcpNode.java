package com.skcc.cloudz.zcp.common.model;

import java.math.BigDecimal;

public class ZcpNode {
	
	private String nodeName;
	private NodeStatus status;
	private BigDecimal cpuRequests;
	private int cpuRequestsPercentage;
	private BigDecimal memoryRequests;
	private int memoryRequestsPercentage;
	private BigDecimal cpuLimits;
	private int cpuLimitsPercentage;
	private BigDecimal memoryLimits;
	private int memoryLimitsPercentage;

	public enum NodeStatus {
		Ready, NotReady
	}
	
	public BigDecimal getCpuRequests() {
		return cpuRequests;
	}

	public void setCpuRequests(BigDecimal cpuRequests) {
		this.cpuRequests = cpuRequests;
	}

	public int getCpuRequestsPercentage() {
		return cpuRequestsPercentage;
	}

	public void setCpuRequestsPercentage(int cpuRequestsPercentage) {
		this.cpuRequestsPercentage = cpuRequestsPercentage;
	}

	public BigDecimal getMemoryRequests() {
		return memoryRequests;
	}

	public void setMemoryRequests(BigDecimal memoryRequests) {
		this.memoryRequests = memoryRequests;
	}

	public int getMemoryRequestsPercentage() {
		return memoryRequestsPercentage;
	}

	public void setMemoryRequestsPercentage(int memoryRequestsPercentage) {
		this.memoryRequestsPercentage = memoryRequestsPercentage;
	}

	public BigDecimal getCpuLimits() {
		return cpuLimits;
	}

	public void setCpuLimits(BigDecimal cpuLimits) {
		this.cpuLimits = cpuLimits;
	}

	public int getCpuLimitsPercentage() {
		return cpuLimitsPercentage;
	}

	public void setCpuLimitsPercentage(int cpuLimitsPercentage) {
		this.cpuLimitsPercentage = cpuLimitsPercentage;
	}

	public BigDecimal getMemoryLimits() {
		return memoryLimits;
	}

	public void setMemoryLimits(BigDecimal memoryLimits) {
		this.memoryLimits = memoryLimits;
	}

	public int getMemoryLimitsPercentage() {
		return memoryLimitsPercentage;
	}

	public void setMemoryLimitsPercentage(int memoryLimitsPercentage) {
		this.memoryLimitsPercentage = memoryLimitsPercentage;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public NodeStatus getStatus() {
		return status;
	}

	public void setStatus(NodeStatus status) {
		this.status = status;
	}

}
