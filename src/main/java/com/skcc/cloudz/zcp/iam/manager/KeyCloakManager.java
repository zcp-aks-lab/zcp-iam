package com.skcc.cloudz.zcp.iam.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.skcc.cloudz.zcp.iam.common.exception.KeyCloakException;
import com.skcc.cloudz.zcp.iam.common.model.CredentialActionType;

@Component
public class KeyCloakManager {

	private final Logger logger = LoggerFactory.getLogger(KeyCloakManager.class);

	public static final String DEFAULT_NAMESPACE_ATTRIBUTE_KEY = "defaultNamespace";

	public static final String NAMESPACES_ATTRIBUTE_KEY = "namespaces";

	@Autowired
	@Qualifier("keycloak")
	private Keycloak keycloak;
	
	@Value("${zcp.keycloak.realm}")
	private String realm;
	
	@Value("${zcp.keycloak.token-url:'/realms/{realm}/protocol/openid-connect/token'}")
	private String tokenUrl;
	
	@Value("${keycloak.serverUrl}")
	private String keycloakUrl;
	
	private RestTemplate restTemplate = new RestTemplate();
	
	public List<UserRepresentation> getUserList() {
		return getUserList(null);
	}

	public List<UserRepresentation> getUserList(String keyword) {
		RealmResource realmResource = keycloak.realm(realm);
		UsersResource usersResoure = realmResource.users();
		if (StringUtils.isEmpty(keyword)) {
			return usersResoure.list();
		} else {
			return usersResoure.search(keyword, 0, Integer.MAX_VALUE);
		}
	}

	public void createUser(UserRepresentation userRepresentation) {
		UsersResource usersRessource = keycloak.realm(realm).users();
		usersRessource.create(userRepresentation);
	}

	public UserRepresentation getUser(String id) throws KeyCloakException {
		UsersResource usersRessource = keycloak.realm(realm).users();
		UserResource userResource = usersRessource.get(id);
		if (userResource == null) {
			throw new KeyCloakException("KK-0001", "The user(" + id + ") does not exist");
		}

		UserRepresentation userRepresentation = null;
		try {
			userRepresentation = userResource.toRepresentation();
		} catch (Exception e) {
			e.printStackTrace();
			throw new KeyCloakException("KK-0001", "The user(" + id + ") does not exist");
		}

		return userRepresentation;
	}

	public void editUser(UserRepresentation userRepresentation) throws KeyCloakException {
		UsersResource usersRessource = keycloak.realm(realm).users();
		// to keep the other's original values
		UserResource userResource = usersRessource.get(userRepresentation.getId());
		UserRepresentation currnetUserRepresentation = userResource.toRepresentation();

		if (currnetUserRepresentation == null) {
			throw new KeyCloakException("KK-0001", "The user does not exist");
		}

		BeanUtils.copyProperties(userRepresentation, currnetUserRepresentation);
		// TODO need to check why the enabled property did not be copied
		currnetUserRepresentation.setEnabled(userRepresentation.isEnabled());
		currnetUserRepresentation.setEmailVerified(userRepresentation.isEmailVerified());
		
		userResource.update(currnetUserRepresentation);
	}

	public void deleteUser(String id) throws KeyCloakException {
		UsersResource usersRessource = keycloak.realm(realm).users();
		UserResource userResource = usersRessource.get(id);
		if (userResource == null) {
			throw new KeyCloakException("KK-0001", "The user does not exist");
		}

		userResource.remove();
	}

	@SuppressWarnings("unused")
	@Deprecated
	private UserRepresentation getUser(UsersResource usersResource, String username) throws KeyCloakException {
		List<UserRepresentation> users = usersResource.search(username);
		if (users == null || users.isEmpty()) {
			throw new KeyCloakException("KK-0001", "The user who username is (" + username + ") does not exist");
		}

		if (users.size() > 1) {
			logger.debug("There are users({}) with same username ({})", users.size(), username);
			for (UserRepresentation userRepresentation : users) {
				if (userRepresentation.getUsername().equals(username)) {
					return userRepresentation;
				}
			}

			throw new KeyCloakException("KK-0001", "The user who username is (" + username + ") does not exist");
		}

		return users.get(0);
	}

	public void editUserPassword(String id, CredentialRepresentation credentail) throws KeyCloakException {
		UsersResource usersRessource = keycloak.realm(realm).users();
		UserResource userResource = usersRessource.get(id);
		UserRepresentation userRepresentation = userResource.toRepresentation();
		if (userRepresentation == null) {
			throw new KeyCloakException("KK-000", "user does not exist");
		}
			
		userResource.resetPassword(credentail);
	}

	public void resetUserCredentials(String id, List<String> actions) throws KeyCloakException {
		UsersResource usersResource = keycloak.realm(realm).users();
		UserResource userResource = usersResource.get(id);

		if (userResource == null) {
			throw new KeyCloakException("KK-000", "user does not exist");
		}

		userResource.executeActionsEmail(actions);

		// UserRepresentation userRepresentation = userResource.toRepresentation();
		// userRepresentation.setRequiredActions(actions);
		// userResource.update(userRepresentation);
	}

	public void enableUserOtpPassword(String id) throws KeyCloakException {
		UsersResource usersResource = keycloak.realm(realm).users();
		UserResource userResource = usersResource.get(id);

		if (userResource == null) {
			throw new KeyCloakException("KK-000", "user does not exist");
		}

		List<String> actions = new ArrayList<>();
		actions.add(CredentialActionType.CONFIGURE_TOTP.name());

		UserRepresentation userRepresentation = userResource.toRepresentation();
		userRepresentation.setRequiredActions(actions);

		userResource.update(userRepresentation);
	}

	public void deleteUserOtpPassword(String id) throws KeyCloakException {
		UsersResource usersResource = keycloak.realm(realm).users();
		UserResource userResource = usersResource.get(id);
		if (userResource == null) {
			throw new KeyCloakException("KK-000", "user does not exist");
		}
		userResource.removeTotp();
	}

	public void logout(String id) throws KeyCloakException {
		UsersResource usersResource = keycloak.realm(realm).users();
		UserResource userResource = usersResource.get(id);
		if (userResource == null) {
			throw new KeyCloakException("KK-000", "user does not exist");
		}
		userResource.logout();
	}
	
	@SuppressWarnings("rawtypes")
	public String getAccessToken(String id, String password) throws KeyCloakException {
		try {
			UserRepresentation user = getUser(id);
			
			MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
			body.add("username", user.getUsername());
			body.add("password", password);
			body.add("grant_type", "password");
			body.add("client_id", "admin-cli");
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			
			HttpEntity<Map<?,?>> entity = new HttpEntity<Map<?,?>>(body, headers);
			
			// For print IO, change log-level(DEBUG) of RestTemplate or org.spring package.  
			ResponseEntity<HashMap> res = restTemplate.exchange(keycloakUrl + tokenUrl, HttpMethod.POST, entity, HashMap.class, realm);
			logger.debug("Success to get access_token.", res);
			return Objects.toString(res.getBody().get("access_token"));
		} catch (HttpStatusCodeException e) {
			if(HttpStatus.valueOf(e.getRawStatusCode()) == HttpStatus.UNAUTHORIZED) {
				throw new KeyCloakException("KK-002", "The password is incorrect.");
			}
			throw e;
		}
	}
}
