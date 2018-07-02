package com.skcc.cloudz.zcp.namespace.vo;

import com.skcc.cloudz.zcp.common.model.ZcpLimitRange;
import com.skcc.cloudz.zcp.common.model.ZcpResourceQuota;

public class NamespaceResourceDetailVO {
	private String namespace;
	private ZcpResourceQuota hard;
	private ZcpResourceQuota used;
	private ZcpLimitRange limitRange;

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

}
