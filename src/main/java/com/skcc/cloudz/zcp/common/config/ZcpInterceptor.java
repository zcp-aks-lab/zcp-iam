package com.skcc.cloudz.zcp.common.config;

import static com.skcc.cloudz.zcp.common.cnst.COMMON.SESSION_TIMEOUT_OBJ;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.skcc.cloudz.zcp.common.exception.ZcpException;

@Component
@Qualifier(value = "httpInterceptor")
public class ZcpInterceptor extends HandlerInterceptorAdapter {
	
	private static final Logger logger = LoggerFactory.getLogger(ZcpInterceptor.class);
	
	@Autowired
	private Environment environment;
	
	
	@Override
	public boolean preHandle(HttpServletRequest request,
							 HttpServletResponse response,
							 Object handler) throws IOException, ZcpException {
		logger.info("================ Before Method");
		if(environment.getActiveProfiles().equals("real")){
			HttpSession session = request.getSession();
			String timeout_obj = (String)session.getAttribute(SESSION_TIMEOUT_OBJ);
			if(timeout_obj == null) {
				throw new ZcpException("E00001");
			}else {
				//임시
				//response.sendRedirect("/main.jsp");
				return true;
			}
		}else {
			return true;
		}
	}
	
	@Override
	public void postHandle( HttpServletRequest request,
							HttpServletResponse response,
							Object handler,
							ModelAndView modelAndView) {
		logger.info("================ Method Executed");
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request,
								HttpServletResponse response, 
								Object handler, 
								Exception ex) {
		logger.info("================ Method Completed");
	}
}