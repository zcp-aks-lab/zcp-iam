package com.skcc.cloudz.zcp.iam.common.model;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ZcpNode {

	private String nodeName;
	private String nodeType;
	private String nodeRoles;
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
	private BigDecimal cpuRequestsPercentage;
	@JsonIgnore
	private BigDecimal memoryRequests;
	@JsonProperty("memoryRequests")
	private String memoryRequestsString;
	private BigDecimal memoryRequestsPercentage;
	@JsonIgnore
	private BigDecimal cpuLimits;
	@JsonProperty("cpuLimits")
	private String cpuLimitsString;
	private BigDecimal cpuLimitsPercentage;
	@JsonIgnore
	private BigDecimal memoryLimits;
	@JsonProperty("memoryLimits")
	private String memoryLimitsString;
	private BigDecimal memoryLimitsPercentage;
	private Date creationTime;

	public BigDecimal getCpuRequests() {
		return cpuRequests;
	}

	public void setCpuRequests(BigDecimal cpuRequests) {
		this.cpuRequests = cpuRequests;
	}

	public BigDecimal getCpuRequestsPercentage() {
		return cpuRequestsPercentage;
	}

	public void setCpuRequestsPercentage(BigDecimal cpuRequestsPercentage) {
		this.cpuRequestsPercentage = cpuRequestsPercentage;
	}

	public BigDecimal getMemoryRequests() {
		return memoryRequests;
	}

	public void setMemoryRequests(BigDecimal memoryRequests) {
		this.memoryRequests = memoryRequests;
	}

	public BigDecimal getMemoryRequestsPercentage() {
		return memoryRequestsPercentage;
	}

	public void setMemoryRequestsPercentage(BigDecimal memoryRequestsPercentage) {
		this.memoryRequestsPercentage = memoryRequestsPercentage;
	}

	public BigDecimal getCpuLimits() {
		return cpuLimits;
	}

	public void setCpuLimits(BigDecimal cpuLimits) {
		this.cpuLimits = cpuLimits;
	}

	public BigDecimal getCpuLimitsPercentage() {
		return cpuLimitsPercentage;
	}

	public void setCpuLimitsPercentage(BigDecimal cpuLimitsPercentage) {
		this.cpuLimitsPercentage = cpuLimitsPercentage;
	}

	public BigDecimal getMemoryLimits() {
		return memoryLimits;
	}

	public void setMemoryLimits(BigDecimal memoryLimits) {
		this.memoryLimits = memoryLimits;
	}

	public BigDecimal getMemoryLimitsPercentage() {
		return memoryLimitsPercentage;
	}

	public void setMemoryLimitsPercentage(BigDecimal memoryLimitsPercentage) {
		this.memoryLimitsPercentage = memoryLimitsPercentage;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	
	public String getNodeType() {
		return nodeType;
	}
	
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
	
	public String getNodeRoles() {
		return nodeRoles;
	}
	
	public void setNodeRoles(String nodeRoles) {
		this.nodeRoles = nodeRoles;
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
