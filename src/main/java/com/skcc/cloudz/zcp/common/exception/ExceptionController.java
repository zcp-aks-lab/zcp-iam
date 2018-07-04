package com.skcc.cloudz.zcp.common.exception;

//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.skcc.cloudz.zcp.common.vo.RtnVO;

import io.kubernetes.client.ApiException;

@ControllerAdvice
public class ExceptionController {

	private final Logger logger = LoggerFactory.getLogger(ExceptionController.class);

	private Properties properties = new Properties();

	public ExceptionController() throws IOException {
		properties.load(getClass().getClassLoader().getResourceAsStream("exception.properties"));
	}

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public Object exceptionResolver(HttpServletRequest req, Exception e) {
		RtnVO vo = new RtnVO();
		logger.debug("UnKnown Error...{}", e);
		if (e instanceof ZcpException) {
			vo.setCode(((ZcpException) e).getCode());
		} else if (e instanceof KeyCloakException) {
			vo.setCode(((KeyCloakException) e).getCode());
		} else if (e instanceof ApiException) {
			vo.setCode(String.valueOf(((ApiException) e).getCode()));
		} else {
			vo.setCode("500");
		}

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
		vo.setCode("K500");// 코드 수정 예정
		vo.setMsg(e.getMessage());
		return vo;
	}

	@ExceptionHandler(ZcpException.class)
	@ResponseBody
	public Object zcpExceptionResolver(HttpServletRequest req, ZcpException e) {
		// String msg = e.getMessage(); //prop.getProperty(e.getCode());
		// logger.debug(msg, e);
		RtnVO vo = new RtnVO(e.getCode(), e.getMessage());
		return vo;
	}

	@ExceptionHandler(KeyCloakException.class)
	@ResponseBody
	public Object KeycloakExceptionResolver(HttpServletRequest req, KeyCloakException e) {
		RtnVO vo = new RtnVO();
		logger.debug("{} : {} ", e.getCode(), e.getMessage());
		vo.setData("");
		vo.setCode(e.getCode());// 코드 수정 예정
		vo.setMsg(e.getMessage());
		return vo;
	}

}
