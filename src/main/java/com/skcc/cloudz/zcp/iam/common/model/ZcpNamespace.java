package com.skcc.cloudz.zcp.iam.common.model;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public class ZcpNamespace {

	private String name;
	private NamespaceStatus status;
	private Date creationDate;
	private int userCount;

	@JsonIgnore
	private BigDecimal hardCpuRequests;
	@JsonIgnore
	private BigDecimal usedCpuRequests;
	@JsonProperty("hardCpuRequests")
	private String hardCpuRequestsString;
	@JsonProperty("usedCpuRequests")
	private String usedCpuRequestsString;
	private BigDecimal cpuRequestsPercentage;

	@JsonIgnore
	private BigDecimal hardMemoryRequests;
	@JsonIgnore
	private BigDecimal usedMemoryRequests;
	@JsonProperty("hardMemoryRequests")
	private String hardMemoryRequestsString;
	@JsonProperty("usedMemoryRequests")
	private String usedMemoryRequestsString;
	private BigDecimal memoryRequestsPercentage;

	@JsonIgnore
	private BigDecimal hardCpuLimits;
	@JsonIgnore
	private BigDecimal usedCpuLimits;
	@JsonProperty("hardCpuLimits")
	private String hardCpuLimitsString;
	@JsonProperty("usedCpuLimits")
	private String usedCpuLimitsString;
	private BigDecimal cpuLimitsPercentage;

	@JsonIgnore
	private BigDecimal hardMemoryLimits;
	@JsonIgnore
	private BigDecimal usedMemoryLimits;
	@JsonProperty("hardMemoryLimits")
	private String hardMemoryLimitsString;
	@JsonProperty("usedMemoryLimits")
	private String usedMemoryLimitsString;
	private BigDecimal memoryLimitsPercentage;

	public enum NamespaceStatus {
		ACTIVE("Active"), TERMINATING("Terminating");
		private String status;

		private NamespaceStatus(String status) {
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
		public static NamespaceStatus getNamespaceStatus(String status) {
			for (NamespaceStatus s : values()) {
				if (s.getStatus().equals(status)) {
					return s;
				}
			}

			throw new IllegalArgumentException("[" + status + "] is invalid");
		}

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public NamespaceStatus getStatus() {
		return status;
	}

	public void setStatus(NamespaceStatus status) {
		this.status = status;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public BigDecimal getHardCpuRequests() {
		return hardCpuRequests;
	}

	public void setHardCpuRequests(BigDecimal hardCpuRequests) {
		this.hardCpuRequests = hardCpuRequests;
	}

	public BigDecimal getUsedCpuRequests() {
		return usedCpuRequests;
	}

	public void setUsedCpuRequests(BigDecimal usedCpuRequests) {
		this.usedCpuRequests = usedCpuRequests;
	}

	public String getHardCpuRequestsString() {
		return hardCpuRequestsString;
	}

	public void setHardCpuRequestsString(String hardCpuRequestsString) {
		this.hardCpuRequestsString = hardCpuRequestsString;
	}

	public String getUsedCpuRequestsString() {
		return usedCpuRequestsString;
	}

	public void setUsedCpuRequestsString(String usedCpuRequestsString) {
		this.usedCpuRequestsString = usedCpuRequestsString;
	}

	public BigDecimal getCpuRequestsPercentage() {
		return cpuRequestsPercentage;
	}

	public void setCpuRequestsPercentage(BigDecimal cpuRequestsPercentage) {
		this.cpuRequestsPercentage = cpuRequestsPercentage;
	}

	public BigDecimal getHardMemoryRequests() {
		return hardMemoryRequests;
	}

	public void setHardMemoryRequests(BigDecimal hardMemoryRequests) {
		this.hardMemoryRequests = hardMemoryRequests;
	}

	public BigDecimal getUsedMemoryRequests() {
		return usedMemoryRequests;
	}

	public void setUsedMemoryRequests(BigDecimal usedMemoryRequests) {
		this.usedMemoryRequests = usedMemoryRequests;
	}

	public String getHardMemoryRequestsString() {
		return hardMemoryRequestsString;
	}

	public void setHardMemoryRequestsString(String hardMemoryRequestsString) {
		this.hardMemoryRequestsString = hardMemoryRequestsString;
	}

	public String getUsedMemoryRequestsString() {
		return usedMemoryRequestsString;
	}

	public void setUsedMemoryRequestsString(String usedMemoryRequestsString) {
		this.usedMemoryRequestsString = usedMemoryRequestsString;
	}

	public BigDecimal getMemoryRequestsPercentage() {
		return memoryRequestsPercentage;
	}

	public void setMemoryRequestsPercentage(BigDecimal memoryRequestsPercentage) {
		this.memoryRequestsPercentage = memoryRequestsPercentage;
	}

	public BigDecimal getHardCpuLimits() {
		return hardCpuLimits;
	}

	public void setHardCpuLimits(BigDecimal hardCpuLimits) {
		this.hardCpuLimits = hardCpuLimits;
	}

	public BigDecimal getUsedCpuLimits() {
		return usedCpuLimits;
	}

	public void setUsedCpuLimits(BigDecimal usedCpuLimits) {
		this.usedCpuLimits = usedCpuLimits;
	}

	public String getHardCpuLimitsString() {
		return hardCpuLimitsString;
	}

	public void setHardCpuLimitsString(String hardCpuLimitsString) {
		this.hardCpuLimitsString = hardCpuLimitsString;
	}

	public String getUsedCpuLimitsString() {
		return usedCpuLimitsString;
	}

	public void setUsedCpuLimitsString(String usedCpuLimitsString) {
		this.usedCpuLimitsString = usedCpuLimitsString;
	}

	public BigDecimal getCpuLimitsPercentage() {
		return cpuLimitsPercentage;
	}

	public void setCpuLimitsPercentage(BigDecimal cpuLimitsPercentage) {
		this.cpuLimitsPercentage = cpuLimitsPercentage;
	}

	public BigDecimal getHardMemoryLimits() {
		return hardMemoryLimits;
	}

	public void setHardMemoryLimits(BigDecimal hardMemoryLimits) {
		this.hardMemoryLimits = hardMemoryLimits;
	}

	public BigDecimal getUsedMemoryLimits() {
		return usedMemoryLimits;
	}

	public void setUsedMemoryLimits(BigDecimal usedMemoryLimits) {
		this.usedMemoryLimits = usedMemoryLimits;
	}

	public String getHardMemoryLimitsString() {
		return hardMemoryLimitsString;
	}

	public void setHardMemoryLimitsString(String hardMemoryLimitsString) {
		this.hardMemoryLimitsString = hardMemoryLimitsString;
	}

	public String getUsedMemoryLimitsString() {
		return usedMemoryLimitsString;
	}

	public void setUsedMemoryLimitsString(String usedMemoryLimitsString) {
		this.usedMemoryLimitsString = usedMemoryLimitsString;
	}

	public BigDecimal getMemoryLimitsPercentage() {
		return memoryLimitsPercentage;
	}

	public void setMemoryLimitsPercentage(BigDecimal memoryLimitsPercentage) {
		this.memoryLimitsPercentage = memoryLimitsPercentage;
	}

	public int getUserCount() {
		return userCount;
	}

	public void setUserCount(int userCount) {
		this.userCount = userCount;
	}

}
