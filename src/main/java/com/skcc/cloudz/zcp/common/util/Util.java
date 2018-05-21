package com.skcc.cloudz.zcp.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class Util {

	@Autowired
	@Qualifier("keycloak")
	Keycloak keycloak;
	
	@Value("${zcp.realm}")
	String realm;

	//email test bundle collection
	public void getEmailTest() {
		keycloak.realm(realm).toRepresentation().isVerifyEmail();
		keycloak.realm(realm).toRepresentation().isLoginWithEmailAllowed();
		keycloak.realm(realm).toRepresentation().setVerifyEmail(true);
		
	}
	
	public static <T> T toJson(String filename) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<T> typeReference = new TypeReference<T>(){};
		InputStream inputStream = TypeReference.class.getResourceAsStream(filename);
		return mapper.readValue(inputStream,typeReference);
	}
	
	public static <T> List<T> asList(T t){
		List<T> list = new ArrayList();
		list.add(t);
		return list;
	}
}
