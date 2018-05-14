package com.skcc.cloudz.zcp.common.exception;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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
	
	final Logger LOG = LoggerFactory.getLogger(ExceptionController.class);
	
	Properties prop= new Properties();
	  
    public ExceptionController() throws IOException{
    	prop.load(getClass().getClassLoader().getResourceAsStream("exception.properties"));
    }
    
    @ExceptionHandler(Exception.class)
	@ResponseBody
	public Object exceptionResolver(HttpServletRequest req, Exception e) { 
		RtnVO vo = new RtnVO();
		LOG.debug("UnKnown Error...{}",e);
		vo.setCode("500");//코드 수정 예정
		return vo; 
	}
    
    @ExceptionHandler(ApiException.class)
	@ResponseBody
	public Object exceptionResolver(HttpServletRequest req, ApiException e) { 
		RtnVO vo = new RtnVO();
		LOG.debug(e.getResponseHeaders().toString());
		LOG.debug(e.getResponseBody());
		LOG.debug(e.getMessage());
		LOG.debug("",e);
		vo.setData(e.getResponseBody());
		vo.setCode("500");//코드 수정 예정
		return vo; 
	}
	
	@ExceptionHandler(ZcpException.class)
	@ResponseBody
	public Object zcpExceptionResolver(HttpServletRequest req, ZcpException e) {
		String msg = prop.getProperty(e.code);
		LOG.debug(msg,e);
		RtnVO vo = new RtnVO(e.code, msg);
		return vo;
	}


}
