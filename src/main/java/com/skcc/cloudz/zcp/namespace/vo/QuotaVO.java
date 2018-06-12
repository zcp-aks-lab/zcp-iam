package com.skcc.cloudz.zcp.namespace.vo;

import java.util.Date;

import org.joda.time.DateTime;

import io.kubernetes.client.models.V1ResourceQuotaStatus;

public class QuotaVO {
	String name;
	String namespace;
	int userCount;
	V1ResourceQuotaStatus status;
	DateTime creationTimestamp;
	int usedMemoryRate;
	int usedCpuRate;
	
	
	public int getUsedMemoryRate() {
		return usedMemoryRate;
	}
	public void setUsedMemoryRate(int usedMemoryRate) {
		this.usedMemoryRate = usedMemoryRate;
	}
	public int getUsedCpuRate() {
		return usedCpuRate;
	}
	public void setUsedCpuRate(int usedCpuRate) {
		this.usedCpuRate = usedCpuRate;
	}
	public int getUserCount() {
		return userCount;
	}
	public void setUserCount(int userCount) {
		this.userCount = userCount;
	}
	
	public DateTime getCreationTimestamp() {
		return creationTimestamp;
	}
	public void setCreationTimestamp(DateTime creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public V1ResourceQuotaStatus getStatus() {
		return status;
	}
	public void setStatus(V1ResourceQuotaStatus status) {
		this.status = status;
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	
	
}
