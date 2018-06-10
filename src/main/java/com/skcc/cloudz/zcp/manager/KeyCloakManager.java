package com.skcc.cloudz.zcp.manager;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.skcc.cloudz.zcp.common.exception.KeyCloakException;
import com.skcc.cloudz.zcp.user.vo.MemberVO;
import com.skcc.cloudz.zcp.user.vo.PassResetVO;
import com.skcc.cloudz.zcp.user.vo.ZcpUser;

@Component
public class KeyCloakManager {

	private final Logger logger = LoggerFactory.getLogger(KeyCloakManager.class);

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

	public void createUser(ZcpUser zcpUser) {
		UsersResource userRessource = keycloak.realm(realm).users();
		UserRepresentation user = new UserRepresentation();
		user.setFirstName(zcpUser.getFirstName());
		user.setLastName(zcpUser.getLastName());
		user.setEmail(zcpUser.getEmail());
		user.setUsername(zcpUser.getUsername());
		user.setEnabled(zcpUser.isEnabled());
		userRessource.create(user);
		// TODO defaultNamesapce
	}

	public ZcpUser getUser(String username) throws KeyCloakException {
		UsersResource userRessource = keycloak.realm(realm).users();
		UserRepresentation userRepresentation = getUser(userRessource, username);

		ZcpUser user = new ZcpUser();
		user.setId(userRepresentation.getId());
		user.setFirstName(userRepresentation.getFirstName());
		user.setLastName(userRepresentation.getLastName());
		user.setEmail(userRepresentation.getEmail());
		user.setEnabled(userRepresentation.isEnabled());
		user.setUsername(userRepresentation.getUsername());
		user.setCreatedDate(new Date(userRepresentation.getCreatedTimestamp()));
		user.setEmailVerified(userRepresentation.isEmailVerified());
		Map<String, List<String>> attributes = userRepresentation.getAttributes();
		if (attributes != null) {
			List<String> defaultNamespaces = attributes.get("defaultNamespace");
			if (defaultNamespaces != null && !defaultNamespaces.isEmpty()) {
				user.setDefaultNamespace(defaultNamespaces.get(0));
			}
		}

		return user;
	}

	public void editUser(MemberVO vo) throws KeyCloakException {
		UsersResource userRessource = keycloak.realm(realm).users();
		UserRepresentation user = getUser(userRessource, vo.getUserName());
		if (user != null) {
			user.setFirstName(vo.getFirstName());
			user.setLastName(vo.getLastName());
			user.setEmail(vo.getEmail());
			user.setAttributes(vo.rcvAttributeMap());
			user.setUsername(vo.getUserName());
			user.setEnabled(vo.getEnabled());
			userRessource.get(user.getId()).update(user);
		}
	}

	public void editAttribute(MemberVO vo) throws KeyCloakException {
		UsersResource userRessource = keycloak.realm(realm).users();
		UserRepresentation user = getUser(userRessource, vo.getUserName());
		if (user != null) {
			userRessource.get(user.getId()).update(user);
		}
	}

	public void deleteUser(String userName) throws KeyCloakException {
		UsersResource userRessource = keycloak.realm(realm).users();
		UserRepresentation user = getUser(userRessource, userName);
		if (user != null) {
			userRessource.get(user.getId()).remove();
		}
	}

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

	public void editUserPassword(MemberVO vo) throws KeyCloakException {
		UsersResource userRessource = keycloak.realm(realm).users();
		UserRepresentation userRepresentation = getUser(userRessource, vo.getUserName());
		if (userRepresentation != null) {
			CredentialRepresentation credentail = new CredentialRepresentation();
			credentail.setType(CredentialRepresentation.PASSWORD);
			credentail.setValue(vo.getPassword());
			credentail.setTemporary(vo.isChangedAfterLogin());
			userRessource.get(userRepresentation.getId()).resetPassword(credentail);
		}
	}

	public void initUserPassword(PassResetVO vo) throws KeyCloakException {
		UsersResource usersResource = keycloak.realm(realm).users();
		UserRepresentation userp = getUser(usersResource, vo.getUserName());
		if (userp != null) {
			UserResource user = usersResource.get(userp.getId());
			// CredentialRepresentation credentialRepresentation = new
			// CredentialRepresentation();
			// credentialRepresentation.setPeriod(vo.getPeriod());
			// //usersResource.get(userp.getId()).update(userp);
			// credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
			// user.resetPassword(credentialRepresentation);
			user.executeActionsEmail(null, vo.getRedirectUri(), vo.getActions());
		}
	}

	public void removeOtpPassword(String username) throws KeyCloakException {
		UsersResource usersResource = keycloak.realm(realm).users();
		UserRepresentation userRepresentation = getUser(usersResource, username);
		if (userRepresentation != null) {
			UserResource user = usersResource.get(userRepresentation.getId());
			user.removeTotp();
		}
	}

	public void logout(String username) throws KeyCloakException {
		UsersResource usersResource = keycloak.realm(realm).users();
		UserRepresentation userRepresentation = getUser(usersResource, username);
		if (userRepresentation != null) {
			UserResource user = usersResource.get(userRepresentation.getId());
			user.logout();
		}
	}

}
