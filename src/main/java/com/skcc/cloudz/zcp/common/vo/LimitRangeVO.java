package com.skcc.cloudz.zcp.common.vo;

import io.kubernetes.client.models.V1LimitRange;

public class LimitRangeVO extends V1LimitRange implements Ivo{
	String namespace;

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
}
