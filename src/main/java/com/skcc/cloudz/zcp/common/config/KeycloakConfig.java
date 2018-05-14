package com.skcc.cloudz.zcp.common.config;

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
	
	final Logger LOG = LoggerFactory.getLogger(KeycloakConfig.class);
	
	@Value("${keycloak.serverUrl}")
	String serverUrl; 
	
	@Value("${keycloak.realm}")
	String realm; 
	
	@Value("${keycloak.clientId}")
	String clientId; 
	
	@Value("${keycloak.clientSecret}")
	String clientSecret; 
	
	@Value("${keycloak.username}")
	String username;
	
	@Value("${keycloak.password}")
	String password;
	
	@Bean
	@Qualifier("keycloak")
	public Keycloak getInstance() {
		LOG.debug("Keycloak init...!!!!!!!!!!!!!!!!!!!!!");
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
