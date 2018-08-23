package com.skcc.cloudz.zcp.iam.common.exception;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.skcc.cloudz.zcp.iam.common.vo.ErrorVO;

import io.kubernetes.client.ApiException;

@ControllerAdvice
public class ExceptionController {

	private final Logger logger = LoggerFactory.getLogger(ExceptionController.class);

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public Object exceptionResolver(HttpServletRequest req, Exception e) {
		logger.debug("UnKnown Error...", e);
		ErrorVO vo = new ErrorVO();
		String code = String.format("%d", ZcpErrorCode.UNKNOWN_ERROR.getCode());
		vo.setCode(code);
		vo.setMsg(ZcpErrorCode.UNKNOWN_ERROR.toString());
		vo.setDetail(e.toString());
		return vo;
	}

	@ExceptionHandler(ApiException.class)
	@ResponseBody
	public Object exceptionResolver(HttpServletRequest req, ApiException e) {
		logger.debug("Kebe Exception...", e);
		ErrorVO vo = new ErrorVO();
		logger.debug(e.getResponseHeaders() == null ? "" : e.getResponseHeaders().toString());
		logger.debug(e.getResponseBody());
		logger.debug(e.getMessage());
		String code = String.format("%d", ZcpErrorCode.KUBERNETES_UNKNOWN_ERROR.getCode());
		vo.setDetail(e.getResponseBody());
		vo.setCode(code);
		vo.setMsg(ZcpErrorCode.KEYCLOAK_UNKNOWN_ERROR.toString());
		
		return vo;
	}

	@ExceptionHandler(ZcpException.class)
	@ResponseBody
	public Object zcpExceptionResolver(HttpServletRequest req, ZcpException e) {
		logger.debug("ZcpException...", e);
		String code = String.format("%d", e.getCode().getCode());
		ErrorVO vo = new ErrorVO();
		vo.setCode(code);
		vo.setMsg(e.getCode().getMessage());
		vo.setDetail(e.getApiMsg());
		return vo;
	}

	@ExceptionHandler(KeyCloakException.class)
	@ResponseBody
	public Object KeycloakExceptionResolver(HttpServletRequest req, KeyCloakException e) {
		logger.debug("KeyCloakException...", e);
		ErrorVO vo = new ErrorVO();
		String code = String.format("%d", ZcpErrorCode.KEYCLOAK_UNKNOWN_ERROR.getCode());
		vo.setCode(code);
		vo.setMsg(ZcpErrorCode.KEYCLOAK_UNKNOWN_ERROR.toString());
		vo.setDetail(e.getMessage());
		return vo;
	}
	

}
