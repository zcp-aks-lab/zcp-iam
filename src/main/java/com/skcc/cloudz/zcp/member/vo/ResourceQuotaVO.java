package com.skcc.cloudz.zcp.member.vo;

import io.kubernetes.client.models.V1ResourceQuota;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "Custom")
public class ResourceQuotaVO extends V1ResourceQuota implements Ivo{
	String namespace;

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
}
