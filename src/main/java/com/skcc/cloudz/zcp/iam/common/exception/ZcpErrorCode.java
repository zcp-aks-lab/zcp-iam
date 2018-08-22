package com.skcc.cloudz.zcp.iam.common.exception;

public enum ZcpErrorCode {
	
	//common
	UNKNOWN_ERROR(10000, "Unknown error"),
	KUBERNETES_ERROR(10001, "Unknown kobernetes error"),
	KEYCLOAK_ERROR(10002, "Unknown keycloak error"),
	USER_NOT_FOUND(10003, ""),
	CLUSTERROLEBINDING_NOT_FOUND(10004, ""),
	PERMISSION_DENY(10005, ""),
	
	//app
	UNAUTHORIZED_ERROR(11001, "Unauthorized error"),
	
	//metric
	UNSUPPORTED_TYPE(21001, "Unsupported type"),
	
	//namespace
	ROLEBINDING_NOT_FOUND(31001, "")
	
	
	;
	
	
	private int code;
	private String message;
	
	private ZcpErrorCode(int code, String message) {
		this.code = code;
		this.message = message;
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}


}
