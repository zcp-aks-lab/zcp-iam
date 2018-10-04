package com.skcc.cloudz.zcp.iam.api.namespace.vo;

import com.skcc.cloudz.zcp.iam.common.vo.Ivo;

public interface SecretVO extends Ivo {
	public String getName();
	public String getType();
	public String getDescription();
}
