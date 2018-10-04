package com.skcc.cloudz.zcp.iam.api.cluster.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import com.google.common.collect.Lists;

public class VerifyContext extends ConcurrentHashMap<String, Object> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected static Class<? extends VerifyContext> contextClass = VerifyContext.class;
	protected static final ThreadLocal<? extends VerifyContext> threadLocal = new ThreadLocal<VerifyContext>() {
        @Override
        protected VerifyContext initialValue() {
            try {
                return contextClass.newInstance();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    };
    
    public static Map<String, Object> getContext() {
    	return threadLocal.get();
    }
    
	public static void setDryRun(boolean dry) {
		VerifyContext ctx = threadLocal.get();
		ctx.put("dry-run", dry);
	}

	public static boolean isDryRun() {
		VerifyContext ctx = threadLocal.get();
		return Boolean.TRUE.equals(ctx.get("dry-run"));
	}

	public static void print(String fmt, Object... args) {
		VerifyContext ctx = threadLocal.get();
		List<Object> log = ctx.getAsList();

		FormattingTuple tuple = MessageFormatter.arrayFormat(fmt, args);
		log.add(tuple.getMessage());
	}
	
	
	private static List<Object> getAsList(){
		VerifyContext ctx = threadLocal.get();

		List<Object> log = (List<Object>) ctx.get("log");
		if(log == null) {
			log = Lists.newArrayList();
			ctx.put("log", log);
		}
		
		return log;
	}
}
