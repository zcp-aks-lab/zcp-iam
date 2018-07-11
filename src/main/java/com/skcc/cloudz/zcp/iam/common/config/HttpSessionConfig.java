package com.skcc.cloudz.zcp.iam.common.config;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpSessionConfig {
	static long count =0;
	
    @Bean
    
    public HttpSessionListener httpSessionListener() {
    	
        return new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent se) {
            	count++;
                System.out.println("Session count" + count);
            }
            
            @Override
            public void sessionDestroyed(HttpSessionEvent se) {
            	count--;
            	System.out.println("Session count" + count);
            }
        };
    }
    
    @Bean                   // bean for http session attribute listener
    public HttpSessionAttributeListener httpSessionAttributeListener() {
        return new HttpSessionAttributeListener() {
            @Override
            public void attributeAdded(HttpSessionBindingEvent se) {            // This method will be automatically called when session attribute added
                System.out.println("Attribute Added following information");
                System.out.println("Attribute Name:" + se.getName());
                System.out.println("Attribute Value:" + se.getName());
            }
            @Override
            public void attributeRemoved(HttpSessionBindingEvent se) {      // This method will be automatically called when session attribute removed
                System.out.println("attributeRemoved");
            }
            @Override
            public void attributeReplaced(HttpSessionBindingEvent se) {     // This method will be automatically called when session attribute replace
                System.out.println("Attribute Replaced following information");
                System.out.println("Attribute Name:" + se.getName());
                System.out.println("Attribute Old Value:" + se.getValue());
            }
        };
    }
}