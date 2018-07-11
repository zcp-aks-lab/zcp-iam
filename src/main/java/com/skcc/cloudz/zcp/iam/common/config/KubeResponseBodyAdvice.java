package com.skcc.cloudz.zcp.iam.common.config;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.skcc.cloudz.zcp.iam.common.annotation.NullProperty;
import com.skcc.cloudz.zcp.iam.common.vo.Response;

@ControllerAdvice 
public class KubeResponseBodyAdvice implements ResponseBodyAdvice<Object> {

	NullProperty property = null; 
	
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		property = returnType.getMethod().getAnnotation(NullProperty.class);
		return property != null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {
		try {
			List<String> fields = Arrays.asList(property.field());
			
			for(String field : fields) {
			//if(fields.stream().anyMatch(data -> "metadata.creationTimestamp".equals(data))) {
				if(field.split("\\.").length == 1) {
					Object data = ((Response<Object>)body).getData();
					if(data == null) continue;
					Field fieldMeta = data.getClass().getDeclaredField(field.split("\\.")[0]);
					fieldMeta.setAccessible(true);
					fieldMeta.set(data, null);
				}
				else if(field.split("\\.").length == 2) {
					Object data = ((Response<Object>)body).getData();
					if(data == null) continue;
					Field field1 = data.getClass().getDeclaredField(field.split("\\.")[0]);
					field1.setAccessible(true);
					Object obj1 = field1.get(data);
					if(obj1 instanceof List){
						List<Object> listObj = (List<Object> )obj1;
						for(Object o : listObj) {
							Field field2 = o.getClass().getDeclaredField(field.split("\\.")[1]);
							field2.setAccessible(true);
							field2.set(o, null);
						}
					}else {
						Field field2 = obj1.getClass().getDeclaredField(field.split("\\.")[1]);
						field2.setAccessible(true);
						field2.set(obj1, null);
					}
					
				}
				else if(field.split("\\.").length == 3) {
					Object data = ((Response<Object>)body).getData();
					if(data == null) continue;
					Field field1 = data.getClass().getDeclaredField(field.split("\\.")[0]);
					field1.setAccessible(true);
					Object obj1 = field1.get(data);
					if(obj1 instanceof List){
						List<Object> listObj = (List<Object> )obj1;
						for(Object o : listObj) {
							Field field2 = o.getClass().getDeclaredField(field.split("\\.")[1]);
							field2.setAccessible(true);
							Object obj2 = field2.get(o);
							Field field3 = obj2.getClass().getDeclaredField(field.split("\\.")[2]);
							field3.setAccessible(true);
							field3.set(obj2, null);
						}
					}else {
						Field field2 = obj1.getClass().getDeclaredField(field.split("\\.")[1]);
						field2.setAccessible(true);
						Object obj2 = field2.get(obj1);
						Field field3 = obj2.getClass().getDeclaredField(field.split("\\.")[2]);
						field3.setAccessible(true);
						field3.set(obj2, null);
					}
				}
			}
		} catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		return body;
	}
	
//	private Field setNull(Object data, String[] name) {
//		
//	}
	
}
