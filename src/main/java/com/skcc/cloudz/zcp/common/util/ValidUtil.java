package com.skcc.cloudz.zcp.common.util;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class ValidUtil {
	public static String required(Object requestParam, String... keys) {
		if(requestParam instanceof Map) {
			for(String key : keys) {
				String value = ((Map)requestParam).get(key).toString();
				if(StringUtils.isEmpty(value)) {
					return "필수 값 : " + key;
				}
			}
		}
		
		return null;
	}
}
