package com.skcc.cloudz.zcp.common.util;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.skcc.cloudz.zcp.member.vo.Ivo;

public class ValidUtil {
	
	public static String EMAIL = "([\\w-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([\\w-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
	
	
	public static String required(Object requestParam, String... keys) {
		if(requestParam instanceof Map) {
			for(String key : keys) {
				try {
					String value = ((Map)requestParam).get(key).toString();
					if(StringUtils.isEmpty(value)) {
						return "필수 값 : " + key;
					}
				}catch(java.lang.NullPointerException e) {
					return "필수 값 : " + key;
				}
			}
		} else if(requestParam instanceof Ivo) {
			Field[] fields = requestParam.getClass().getDeclaredFields();
			int c=0;
			for(Field field : fields) {
				for(String key : keys) {
					if(field.getName().equals(key)) {
						c++;
						try {
							field.setAccessible(true);
							if(StringUtils.isEmpty((String)field.get(requestParam))) {
								return "필수 값 : " + key;
							}
						} catch (IllegalArgumentException | IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			if(c == 0) return "필수 값 : " + keys[0];
		}
		
		return null;
	}
	
	public static boolean check(String regex, String value) {
		return Pattern.matches(regex, value.trim());
	}
}
