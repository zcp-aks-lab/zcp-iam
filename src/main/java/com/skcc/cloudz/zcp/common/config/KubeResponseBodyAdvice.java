package com.skcc.cloudz.zcp.common.config;

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

import com.skcc.cloudz.zcp.common.annotation.RemoveProperty;
import com.skcc.cloudz.zcp.common.vo.Response;

import io.kubernetes.client.models.V1ObjectMeta;

@ControllerAdvice 
public class KubeResponseBodyAdvice implements ResponseBodyAdvice<Object> {

	RemoveProperty property = null; 
	
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		property = returnType.getMethod().getAnnotation(RemoveProperty.class);
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
					Field fieldMeta = data.getClass().getDeclaredField(field.split("\\.")[0]);
					fieldMeta.setAccessible(true);
					fieldMeta.set(data, null);
				}
				else if(field.split("\\.").length == 2) {
					Object data = ((Response<Object>)body).getData();
					Field fieldMeta = data.getClass().getDeclaredField(field.split("\\.")[0]);
					fieldMeta.setAccessible(true);
					V1ObjectMeta metadata = (V1ObjectMeta)fieldMeta.get(data);
					Field timeField = metadata.getClass().getDeclaredField(field.split("\\.")[1]);
					timeField.setAccessible(true);
					timeField.set(metadata, null);
				}
			}
		} catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		return body;
	} 
	
}
