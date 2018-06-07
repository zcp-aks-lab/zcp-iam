package com.skcc.cloudz.zcp.common.exception;

public class KeycloakException extends Exception {

	private static final long serialVersionUID = -9187848309581965198L;

	String msg;
	String code;
	
	public KeycloakException(String code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public KeycloakException(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMsg() {
		return msg;
	}
}
