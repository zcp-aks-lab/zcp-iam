package com.skcc.cloudz.zcp.common.exception;

public enum ZcpErrorCode {
	SUCCESS(0, "Success"),
	USER_NOTFOUND(10001, "User not found");
	
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
