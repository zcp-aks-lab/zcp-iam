package com.skcc.cloudz.zcp.manager;

import java.util.List;

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
import org.springframework.stereotype.Component;

import com.skcc.cloudz.zcp.common.exception.KeyCloakException;

@Component
public class KeyCloakManager {

	private final Logger logger = LoggerFactory.getLogger(KeyCloakManager.class);

	public static final String DEFAULT_NAMESPACE_ATTRIBUTE_KEY = "defaultNamespace";

	@Autowired
	@Qualifier("keycloak")
	private Keycloak keycloak;

	@Value("${zcp.realm}")
	private String realm;

	@Value("${keycloak.clientId}")
	private String clientId;

	public List<UserRepresentation> getUserList() {
		RealmResource realmResource = keycloak.realm(realm);
		return realmResource.users().list();
	}

	public void createUser(UserRepresentation userRepresentation) {
		UsersResource usersRessource = keycloak.realm(realm).users();
		usersRessource.create(userRepresentation);
	}

	public UserRepresentation getUser(String id) throws KeyCloakException {
		UsersResource usersRessource = keycloak.realm(realm).users();
		UserResource userResource = usersRessource.get(id);
		if (userResource == null) {
			throw new KeyCloakException("KK-0001", "The user does not exist");
		}
		
		return userResource.toRepresentation();
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

}
