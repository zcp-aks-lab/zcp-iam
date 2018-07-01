package com.skcc.cloudz.zcp.common.model;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public class ZcpNode {

	private String nodeName;
	private NodeStatus status;
	@JsonIgnore
	private BigDecimal allocatableCpu;
	@JsonProperty("allocatableCpu")
	private String allocatableCpuString;
	@JsonIgnore
	private BigDecimal allocatableMemory;
	@JsonProperty("allocatableMemory")
	private String allocatableMemoryString;

	@JsonIgnore
	private BigDecimal cpuRequests;
	@JsonProperty("cpuRequests")
	private String cpuRequestsString;
	private int cpuRequestsPercentage;
	@JsonIgnore
	private BigDecimal memoryRequests;
	@JsonProperty("memoryRequests")
	private String memoryRequestsString;
	private int memoryRequestsPercentage;
	@JsonIgnore
	private BigDecimal cpuLimits;
	@JsonProperty("cpuLimits")
	private String cpuLimitsString;
	private int cpuLimitsPercentage;
	@JsonIgnore
	private BigDecimal memoryLimits;
	@JsonProperty("memoryLimits")
	private String memoryLimitsString;
	private int memoryLimitsPercentage;
	private Date creationTime;

	public enum NodeStatus {
		READY("Ready"), NOT_READY("NotReady"), UNKNOWN("Unknown");
		private String status;

		private NodeStatus(String status) {
			this.status = status;
		}

		@JsonValue
		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		@JsonCreator
		public static NodeStatus getNodeStatus(String status) {
			for (NodeStatus s : values()) {
				if (s.getStatus().equals(status)) {
					return s;
				}
			}

			throw new IllegalArgumentException("[" + status + "] is invalid");
		}

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

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public String getCpuRequestsString() {
		return cpuRequestsString;
	}

	public void setCpuRequestsString(String cpuRequestsString) {
		this.cpuRequestsString = cpuRequestsString;
	}

	public String getMemoryRequestsString() {
		return memoryRequestsString;
	}

	public void setMemoryRequestsString(String memoryRequestsString) {
		this.memoryRequestsString = memoryRequestsString;
	}

	public String getCpuLimitsString() {
		return cpuLimitsString;
	}

	public void setCpuLimitsString(String cpuLimitsString) {
		this.cpuLimitsString = cpuLimitsString;
	}

	public String getMemoryLimitsString() {
		return memoryLimitsString;
	}

	public void setMemoryLimitsString(String memoryLimitsString) {
		this.memoryLimitsString = memoryLimitsString;
	}

	public BigDecimal getAllocatableCpu() {
		return allocatableCpu;
	}

	public void setAllocatableCpu(BigDecimal allocatableCpu) {
		this.allocatableCpu = allocatableCpu;
	}

	public String getAllocatableCpuString() {
		return allocatableCpuString;
	}

	public void setAllocatableCpuString(String allocatableCpuString) {
		this.allocatableCpuString = allocatableCpuString;
	}

	public BigDecimal getAllocatableMemory() {
		return allocatableMemory;
	}

	public void setAllocatableMemory(BigDecimal allocatableMemory) {
		this.allocatableMemory = allocatableMemory;
	}

	public String getAllocatableMemoryString() {
		return allocatableMemoryString;
	}

	public void setAllocatableMemoryString(String allocatableMemoryString) {
		this.allocatableMemoryString = allocatableMemoryString;
	}

}
