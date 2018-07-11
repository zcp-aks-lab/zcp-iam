package com.skcc.cloudz.zcp.iam.common.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.skcc.cloudz.zcp.iam.common.util.NumberUtils;

public class ZcpResourceQuotaUsage {
	@JsonIgnore
	private ZcpResourceQuota hard;
	@JsonIgnore
	private ZcpResourceQuota used;

	public ZcpResourceQuotaUsage() {
		super();
	}

	public ZcpResourceQuotaUsage(ZcpResourceQuota used, ZcpResourceQuota hard) {
		this.used = used;
		this.hard = hard;
	}

	public ZcpResourceQuota getHard() {
		return hard;
	}

	public void setHard(ZcpResourceQuota hard) {
		this.hard = hard;
	}

	public ZcpResourceQuota getUsed() {
		return used;
	}

	public void setUsed(ZcpResourceQuota used) {
		this.used = used;
	}

	public BigDecimal getCpuLimitsUsage() {
		double usedValue = getUsageValue(used.getCpuLimits(), used.getCpuLimitsUnit());
		double hardValue = getUsageValue(hard.getCpuLimits(), hard.getCpuLimitsUnit());
		return NumberUtils.percent(usedValue, hardValue);
	}

	public BigDecimal getCpuRequestsUsage() {
		double usedValue = getUsageValue(used.getCpuRequests(), used.getCpuRequestsUnit());
		double hardValue = getUsageValue(hard.getCpuRequests(), hard.getCpuRequestsUnit());
		return NumberUtils.percent(usedValue, hardValue);
	}

	public BigDecimal getMemoryLimitsUsage() {
		double usedValue = getUsageValue(used.getMemoryLimits(), used.getMemoryLimitsUnit());
		double hardValue = getUsageValue(hard.getMemoryLimits(), hard.getMemoryLimitsUnit());
		return NumberUtils.percent(usedValue, hardValue);
	}

	public BigDecimal getMemoryRequestsUsage() {
		double usedValue = getUsageValue(used.getMemoryRequests(), used.getMemoryRequestsUnit());
		double hardValue = getUsageValue(hard.getMemoryRequests(), hard.getMemoryRequestsUnit());
		return NumberUtils.percent(usedValue, hardValue);
	}

	public BigDecimal getConfigmapsUsage() {
		double usedValue = getUsageValue(used.getConfigmaps());
		double hardValue = getUsageValue(hard.getConfigmaps());
		return NumberUtils.percent(usedValue, hardValue);
	}

	public BigDecimal getPersistentvolumeclaimsUsage() {
		double usedValue = getUsageValue(used.getPersistentvolumeclaims());
		double hardValue = getUsageValue(hard.getPersistentvolumeclaims());
		return NumberUtils.percent(usedValue, hardValue);
	}

	public BigDecimal getPodsUsage() {
		double usedValue = getUsageValue(used.getPods());
		double hardValue = getUsageValue(hard.getPods());
		return NumberUtils.percent(usedValue, hardValue);
	}

	public BigDecimal getReplicationcontrollersUsage() {
		double usedValue = getUsageValue(used.getReplicationcontrollers());
		double hardValue = getUsageValue(hard.getReplicationcontrollers());
		return NumberUtils.percent(usedValue, hardValue);
	}

	public BigDecimal getResourcequotasUsage() {
		double usedValue = getUsageValue(used.getResourcequotas());
		double hardValue = getUsageValue(hard.getResourcequotas());
		return NumberUtils.percent(usedValue, hardValue);
	}

	public BigDecimal getServicesUsage() {
		double usedValue = getUsageValue(used.getServices());
		double hardValue = getUsageValue(hard.getServices());
		return NumberUtils.percent(usedValue, hardValue);
	}

	public BigDecimal getServicesLoadbalancersUsage() {
		double usedValue = getUsageValue(used.getServicesLoadbalancers());
		double hardValue = getUsageValue(hard.getServicesLoadbalancers());
		return NumberUtils.percent(usedValue, hardValue);
	}

	public BigDecimal getServicesNodeportsUsage() {
		double usedValue = getUsageValue(used.getServicesNodeports());
		double hardValue = getUsageValue(hard.getServicesNodeports());
		return NumberUtils.percent(usedValue, hardValue);
	}

	public BigDecimal getSecretsUsage() {
		double usedValue = getUsageValue(used.getSecrets());
		double hardValue = getUsageValue(hard.getSecrets());
		return NumberUtils.percent(usedValue, hardValue);
	}
	
	private double getUsageValue(Number number) {
		if (number == null) return 0;
		return number.doubleValue();
		
	}
	
	private double getUsageValue(Number number, CPUUnit unit) {
		if (number == null) return 0;
		if (unit.equals(CPUUnit.Core)) {
			return number.doubleValue() * 1000;
		}
		return number.doubleValue();
		
	}
	
	private double getUsageValue(Number number, MemoryUnit unit) {
		if (number == null) return 0;
		if (unit == null) return 0;
		
		if (unit.equals(MemoryUnit.Gi)) {
			return number.doubleValue() * 1024;
		}
		return number.doubleValue();
		
	}
}
