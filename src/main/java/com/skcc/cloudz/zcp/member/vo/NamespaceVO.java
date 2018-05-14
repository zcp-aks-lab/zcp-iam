package com.skcc.cloudz.zcp.member.vo;

import io.kubernetes.client.models.V1LimitRange;
import io.kubernetes.client.models.V1ResourceQuota;

public class NamespaceVO  implements Ivo{
	String namespace;
	V1ResourceQuota resourceQuota;
	V1LimitRange limitRange;
	
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public V1ResourceQuota getResourceQuota() {
		return resourceQuota;
	}

	public void setResourceQuota(V1ResourceQuota resourceQuota) {
		this.resourceQuota = resourceQuota;
	}

	public V1LimitRange getLimitRange() {
		return limitRange;
	}

	public void setLimitRange(V1LimitRange limitRange) {
		this.limitRange = limitRange;
	}
	
}
