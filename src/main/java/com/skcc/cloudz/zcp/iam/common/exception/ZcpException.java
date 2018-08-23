package com.skcc.cloudz.zcp.iam.common.exception;

import io.kubernetes.client.ApiException;

public class ZcpException extends Exception {
	
	private static final long serialVersionUID = 4608131642086831206L;
	
	private ZcpErrorCode code;
	private String apiMsg; 
	
	public ZcpException(ZcpErrorCode code) {
		this.code = code;
	}
	
	public ZcpException(ZcpErrorCode code, String message) {
		this.code = code;
		this.code.setMessage(message);
	}
	
	
	public ZcpException(ZcpErrorCode code, ApiException e) {
		super(e);
		this.code = code;
		apiMsg = e.getResponseBody();
	}
	
	public ZcpException(ZcpErrorCode code, KeyCloakException e) {
		super(e);
		this.code = code;
		apiMsg = e.getMessage();
	}
	
	public ZcpErrorCode getCode() {
		return code;
	}
	
	public String getMessage() {
		return this.code.getMessage();
	}

	public String getApiMsg() {
		return apiMsg;
	}

	public void setApiMsg(String apiMsg) {
		this.apiMsg = apiMsg;
	}
	
	
}
