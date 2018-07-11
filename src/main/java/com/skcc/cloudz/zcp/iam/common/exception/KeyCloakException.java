package com.skcc.cloudz.zcp.iam.common.exception;

public class KeyCloakException extends Exception {

	private static final long serialVersionUID = -9187848309581965198L;

	private String message;
	private String code;
	
	public KeyCloakException(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public KeyCloakException(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
}
