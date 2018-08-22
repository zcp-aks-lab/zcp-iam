package com.skcc.cloudz.zcp.iam.common.exception;

import io.kubernetes.client.ApiException;

public class ZcpException extends Exception {
	
	private static final long serialVersionUID = 4608131642086831206L;
	
	private String message;
	private String _code;
	private ZcpErrorCode code;
	Throwable throwable;
	
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
		this.code.setMessage(e.getResponseBody());
	}
	
	public ZcpException(ZcpErrorCode code, Throwable throwable) {
		super(throwable);
		this.code = code;
		this.code.setMessage(message);
	}
	
	public ZcpException(String code, String message) {
		this._code = code;
		this.message = message;
	}
	
	public ZcpException(String code) {
		this._code = code;
	}
	
	public String getCode2() {
		return _code;
	}
	
	public ZcpErrorCode getCode() {
		return code;
	}
	
	public String getMessage() {
		return this.code.getMessage();
	}
}
