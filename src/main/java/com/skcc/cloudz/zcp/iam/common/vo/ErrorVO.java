package com.skcc.cloudz.zcp.iam.common.vo;

import com.skcc.cloudz.zcp.iam.common.exception.ZcpErrorCode;

public class ErrorVO {
	String msg = "Success";
	String code = "200";
	Object detail;
	
	public ErrorVO() {}
	
	public ErrorVO(String code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Object getDetail() {
		return detail;
	}

	public void setDetail(Object detail) {
		this.detail = detail;
	}

	public void setCode(ZcpErrorCode code) {
		this.code = String.format("%d", code.getCode());
	}
	
}
