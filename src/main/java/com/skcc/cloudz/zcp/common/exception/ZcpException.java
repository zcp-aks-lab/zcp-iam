package com.skcc.cloudz.zcp.common.exception;

import org.springframework.web.bind.annotation.ResponseBody;

public class ZcpException extends Exception {
	String msg;
	String code;
	
	public ZcpException(String code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public ZcpException(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMsg() {
		return msg;
	}
}
