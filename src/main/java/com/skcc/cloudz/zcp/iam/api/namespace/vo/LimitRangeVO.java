package com.skcc.cloudz.zcp.iam.api.namespace.vo;

import com.skcc.cloudz.zcp.iam.common.vo.Ivo;

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
