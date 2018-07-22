package com.skcc.cloudz.zcp.iam.common.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class KeycloakConfig{
	
	private final Logger log = LoggerFactory.getLogger(KeycloakConfig.class);
	
	@Value("${keycloak.serverUrl}")
	private String serverUrl; 
	
	@Value("${keycloak.master.realm}")
	private String realm; 
	
	@Value("${keycloak.master.clientId}")
	private String clientId; 
	
	@Value("${keycloak.master.clientSecret}")
	private String clientSecret; 
	
	@Value("${keycloak.master.username}")
	private String username;
	
	@Value("${keycloak.master.password}")
	private String password;
	
	@Bean
	@Qualifier("keycloak")
	public Keycloak getInstance() {
		log.debug("Keycloak init...!!!!!!!!!!!!!!!!!!!!!");
		return KeycloakBuilder.builder() //
				.serverUrl(serverUrl) //
				.realm(realm) //
				.grantType(OAuth2Constants.PASSWORD) //
				.clientId(clientId) //
				.clientSecret(clientSecret) //
				.username(username) //
				.password(password) //
				.build();
	}
}
