package com.skcc.cloudz.zcp.common.exception;

public class ZcpException extends Exception {
	
	private static final long serialVersionUID = 4608131642086831206L;
	
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
