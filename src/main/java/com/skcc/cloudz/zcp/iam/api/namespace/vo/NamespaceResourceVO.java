package com.skcc.cloudz.zcp.iam.api.namespace.vo;

import javax.validation.constraints.NotNull;

import com.skcc.cloudz.zcp.iam.common.model.ZcpLimitRange;
import com.skcc.cloudz.zcp.iam.common.model.ZcpResourceQuota;

public class NamespaceResourceVO {
	@NotNull
	private String namespace;
	private ZcpResourceQuota resourceQuota;
	private ZcpLimitRange limitRange;
	private Boolean zdbAdmin;

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public ZcpResourceQuota getResourceQuota() {
		return resourceQuota;
	}

	public void setResourceQuota(ZcpResourceQuota resourceQuota) {
		this.resourceQuota = resourceQuota;
	}

	public ZcpLimitRange getLimitRange() {
		return limitRange;
	}

	public void setLimitRange(ZcpLimitRange limitRange) {
		this.limitRange = limitRange;
	}

    public Boolean getZdbAdmin() {
        return zdbAdmin;
    }

    public void setZdbAdmin(Boolean zdbAdmin) {
        this.zdbAdmin = zdbAdmin;
    }

}
