package com.skcc.cloudz.zcp.iam.common.exception;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.skcc.cloudz.zcp.iam.common.vo.RtnVO;

import io.kubernetes.client.ApiException;

@ControllerAdvice
public class ExceptionController {

	private final Logger logger = LoggerFactory.getLogger(ExceptionController.class);

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public Object exceptionResolver(HttpServletRequest req, Exception e) {
		RtnVO vo = new RtnVO();
		logger.debug("UnKnown Error...{}", e);
		vo.setCode(ZcpErrorCode.UNKNOWN_ERROR);
		vo.setMsg(e.getMessage());
		return vo;
	}

	@ExceptionHandler(ApiException.class)
	@ResponseBody
	public Object exceptionResolver(HttpServletRequest req, ApiException e) {
		RtnVO vo = new RtnVO();
		logger.debug(e.getResponseHeaders() == null ? "" : e.getResponseHeaders().toString());
		logger.debug(e.getResponseBody());
		logger.debug(e.getMessage());
		logger.debug("", e);
		vo.setData(e.getResponseBody());
		vo.setCode(ZcpErrorCode.KUBERNETES_ERROR);
		vo.setMsg(e.getMessage());
		return vo;
	}

	@ExceptionHandler(ZcpException.class)
	@ResponseBody
	public Object zcpExceptionResolver(HttpServletRequest req, ZcpException e) {
		String code = String.format("%d", e.getCode().getCode());
		RtnVO vo = new RtnVO(code , e.getMessage());
		return vo;
	}

	@ExceptionHandler(KeyCloakException.class)
	@ResponseBody
	public Object KeycloakExceptionResolver(HttpServletRequest req, KeyCloakException e) {
		RtnVO vo = new RtnVO();
		vo.setCode(ZcpErrorCode.KEYCLOAK_ERROR);
		vo.setMsg(e.getMessage());
		return vo;
	}
	

}
