package com.skcc.cloudz.zcp.common.util;

import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
}
