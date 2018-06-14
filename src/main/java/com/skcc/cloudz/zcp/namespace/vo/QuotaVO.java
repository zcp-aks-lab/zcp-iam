package com.skcc.cloudz.zcp.namespace.vo;

import java.util.List;

import org.joda.time.DateTime;

import io.kubernetes.client.models.V1ResourceQuotaStatus;

public class QuotaVO {
	String name;
	String namespace;
	int userCount;
	V1ResourceQuotaStatus status;
	DateTime creationTimestamp;
	double usedMemoryRate;
	double usedCpuRate;
	String active;
	List<String> labels;
	
	
	public List<String> getLabels() {
		return labels;
	}
	public void setLabels(List<String> labels) {
		this.labels = labels;
	}
	public String getActive() {
		return active;
	}
	public void setActive(String active) {
		this.active = active;
	}
	public double getUsedMemoryRate() {
		return usedMemoryRate;
	}
	public void setUsedMemoryRate(double usedMemoryRate) {
		this.usedMemoryRate = usedMemoryRate;
	}
	public double getUsedCpuRate() {
		return usedCpuRate;
	}
	public void setUsedCpuRate(double usedCpuRate) {
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
