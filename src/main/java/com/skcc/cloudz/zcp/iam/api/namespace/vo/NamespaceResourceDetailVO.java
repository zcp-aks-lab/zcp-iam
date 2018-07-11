package com.skcc.cloudz.zcp.iam.api.namespace.vo;

import com.skcc.cloudz.zcp.iam.common.model.ZcpLimitRange;
import com.skcc.cloudz.zcp.iam.common.model.ZcpResourceQuota;
import com.skcc.cloudz.zcp.iam.common.model.ZcpResourceQuotaUsage;

public class NamespaceResourceDetailVO {
	private String namespace;
	private ZcpResourceQuota hard;
	private ZcpResourceQuota used;
	private ZcpLimitRange limitRange;
	private ZcpResourceQuotaUsage usage;

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
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

	public ZcpLimitRange getLimitRange() {
		return limitRange;
	}

	public void setLimitRange(ZcpLimitRange limitRange) {
		this.limitRange = limitRange;
	}

	public ZcpResourceQuotaUsage getUsage() {
		if (usage == null) {
			if (used != null && hard != null) {
				usage = new ZcpResourceQuotaUsage(used, hard);
			}
		}
		return usage;
	}

	public void setUsage(ZcpResourceQuotaUsage usage) {
		this.usage = usage;
	}

}
