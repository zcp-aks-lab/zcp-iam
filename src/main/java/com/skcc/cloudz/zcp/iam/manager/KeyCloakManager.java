package com.skcc.cloudz.zcp.iam.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.skcc.cloudz.zcp.iam.common.exception.KeyCloakException;
import com.skcc.cloudz.zcp.iam.common.model.CredentialActionType;

@Component
public class KeyCloakManager {

	private final Logger logger = LoggerFactory.getLogger(KeyCloakManager.class);

	public static final String DEFAULT_NAMESPACE_ATTRIBUTE_KEY = "defaultNamespace";

	public static final String NAMESPACES_ATTRIBUTE_KEY = "namespaces";
	
	public static final String ZDB_ADMIN_ATTRIBUTE_KEY = "zdb-admin";

	@Autowired
	@Qualifier("keycloak")
	private Keycloak keycloak;
	
	@Value("${zcp.keycloak.realm}")
	private String realm;
	
	@Value("${zcp.keycloak.token-url}")
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
			if(HttpStatus.valueOf(e.getRawStatusCode()) == HttpStatus.UNAUTHORIZED)
				throw new KeyCloakException("KK-002", "The password is incorrect.");
			throw e;
		}
	}
	
	public void createRealmRoles(Collection<String> collection){
		RolesResource roles = keycloak.realm(realm).roles();
		collection.stream()
			.map(name -> new RoleRepresentation(name, "", false))
			.forEach(role -> {
				try {
					roles.create(role);
				} catch (ClientErrorException e) {
					if(e.getResponse().getStatus() == 409) { /*skip*/ }
					else { throw e; }
				}
			});
	}
	
	public void deleteRealmRoles(Collection<String> names){
		RolesResource roles = keycloak.realm(realm).roles();
		names.stream().forEach(name -> roles.deleteRole(name));
	}
	
	public void addRealmRoles(String username, List<String> realmRoles, String tag) throws KeyCloakException {
		// https://www.keycloak.org/docs-api/2.5/rest-api/index.html#_add_a_set_of_realm_level_roles_to_the_client_s_scope
		// https://gist.github.com/thomasdarimont/c4e739c5a319cf78a4cff3b87173a84b
		UserResource user = getUserFromName(username);
		RoleScopeResource roles = user.roles().realmLevel();
		List<RoleRepresentation> rolesToAdd = this.getRealmRoles(realmRoles, true);
		roles.add(rolesToAdd);
		
		logger.debug("Add Roles :: {}", realmRoles);
		
		UserRepresentation data = user.toRepresentation();
		Map<String, List<String>> attr = ObjectUtils.defaultIfNull(data.getAttributes(), new HashMap<String, List<String>>());
		attr.put("role-" + tag, realmRoles);
		data.setAttributes(attr);
		this.editUser(data);
	}

	public void deleteRealmRoles(String username, List<String> realmRoles, String tag) throws KeyCloakException {
		UserResource user = getUserFromName(username);
		UserRepresentation data = user.toRepresentation();

		// remove deleted roles from User Meta
		Map<String, List<String>> attr = ObjectUtils.defaultIfNull(data.getAttributes(), new HashMap<String, List<String>>());
		attr.remove("role-" + tag);
		data.setAttributes(attr);
		
		// merge all namespace's realm-roles
		Set<String> remainRoles = attr.entrySet().stream()
			.filter(e -> e.getKey().startsWith("role-"))
			.flatMap(e -> e.getValue().stream())
			.collect(Collectors.toSet());
		
		// exclude remain roles from deletion
		List<String> deleteRoles = Lists.newArrayList(realmRoles);
		deleteRoles.removeAll(remainRoles);

		logger.debug("Deleted Roles :: {} = {} - {}", deleteRoles, realmRoles, remainRoles);

		// remove actual deleted roles
		RoleScopeResource roles = user.roles().realmLevel();
		List<RoleRepresentation> rolesToDelete = this.getRealmRoles(deleteRoles, false);
		roles.remove(rolesToDelete);

		this.editUser(data);
	}
	
	public List<RoleRepresentation> getRealmRoles(List<String> realmRoles, boolean create) {
		logger.trace("Convert realm-roles-name to RoleRepresentation. [names={}]", realmRoles);
		RolesResource roles = keycloak.realm(realm).roles();
		return realmRoles.stream()
				.map(name -> {
					try {
						return roles.get(name).toRepresentation();
					} catch(NotFoundException e) {
						if(create) {
							// create needed realm role
							logger.info("Create new RoleRepresentation '{}'.", name);
							this.createRealmRoles(Lists.newArrayList(name));
							return roles.get(name).toRepresentation();
						}
						throw e;
					} catch (ClientErrorException e) {
						logger.error("Fail to get RoleRepresentation with '{}'.", name);
						throw e;
					}
				})
				.collect(Collectors.toList());
	}
	
	public UserResource getUserFromName(String username) throws KeyCloakException {
		UsersResource users = keycloak.realm(realm).users();
		List<UserRepresentation> search = this.getUserList(username);
		
		if(search == null || search.size() == 0)
			throw new KeyCloakException("KK-0001", "The user(" + username + ") does not exist");
		
		List<String> cand = search.stream()
			.filter(user -> user.getUsername().equals(username))
			.map(user -> user.getId())
			.collect(Collectors.toList());
		
		if(cand.size() != 1)
			throw new KeyCloakException("KK-0001", "The user(" + username + ") does not exist");
		

		return users.get(cand.get(0));
	}
	
	public void updateUserAttribute(String id, String key, String value) throws KeyCloakException {
	    UsersResource usersRessource = keycloak.realm(realm).users();
        UserResource userResource = usersRessource.get(id);
        
        UserRepresentation userRepresentation = userResource.toRepresentation();
        userRepresentation.setId(id);
        
        Map<String, List<String>> attributes = ObjectUtils.defaultIfNull(userRepresentation.getAttributes(), new HashMap<String, List<String>>());
        attributes.put(key, Arrays.asList(value));
        
        userRepresentation.setAttributes(attributes);
        
        userResource.update(userRepresentation);
	}

}
