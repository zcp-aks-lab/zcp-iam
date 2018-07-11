package com.skcc.cloudz.zcp.iam.common.exception;

public class ZcpException extends Exception {
	
	private static final long serialVersionUID = 4608131642086831206L;
	
	private String message;
	private String code;
	
	public ZcpException(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public ZcpException(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
}
