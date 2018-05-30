package com.skcc.cloudz.zcp.user.dao;

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

import com.skcc.cloudz.zcp.common.exception.ZcpException;
import com.skcc.cloudz.zcp.user.vo.MemberVO;

@Component
public class UserKeycloakDao {

	private static final Logger LOG =  LoggerFactory.getLogger(UserKeycloakDao.class);    
	   
	@Autowired
	@Qualifier("keycloak")
	Keycloak keycloak;
	
	@Value("${zcp.realm}")
	String realm;
	
	@Value("${keycloak.clientId}")
	String clientId;
	
	public List<UserRepresentation> getUserList(){
		RealmResource realmResource = keycloak.realm(realm);
		return realmResource.users().list();
	}

	
	public void deleteUser(String userName) throws ZcpException{
		UsersResource userRessource = keycloak.realm(realm).users();
		List<UserRepresentation> users = userRessource.search(userName);
		if(users != null && users.size() > 0) {
			userRessource.get(users.get(0).getId()).remove();
		}else {
			throw new ZcpException("E00003");
		}
	}
	
	public void editAttribute(MemberVO vo) throws ZcpException{
		UsersResource userRessource = keycloak.realm(realm).users();
		List<UserRepresentation> users = userRessource.search(vo.getUserName());
		if(users != null && users.size() > 0) {
			UserRepresentation user = users.get(0);
			//user.setAttributes(vo.getAttribute());
			userRessource.get(user.getId()).update(user);
		}else {
			throw new ZcpException("E00003");
		}
	}
	
	
	public void createUser(MemberVO vo){
		UsersResource userRessource = keycloak.realm(realm).users();
		UserRepresentation user = new UserRepresentation();
		user.setFirstName(vo.getFirstName());
		user.setLastName(vo.getLastName());
		user.setEmail(vo.getEmail());
		//user.setAttributes(vo.getAttribute());
		user.setUsername(vo.getUserName());
		user.setEnabled(vo.getEnabled());
		userRessource.create(user);
		
	}
	
	
	public void editUser(MemberVO vo) throws ZcpException{
		UsersResource userRessource = keycloak.realm(realm).users();
		List<UserRepresentation> users = userRessource.search(vo.getUserName());
		if(users != null && users.size() > 0) {
			UserRepresentation user = users.get(0);
			user.setFirstName(vo.getFirstName());
			user.setLastName(vo.getLastName());
			user.setEmail(vo.getEmail());
			//user.setAttributes(vo.getAttribute());
			user.setUsername(vo.getUserName());
			user.setEnabled(vo.getEnabled());
			userRessource.get(user.getId()).update(user);
		}else {
			throw new ZcpException("E00003");
		}
	}
	
	
	
	
	
	
	public void editUserPassword(MemberVO vo) throws ZcpException{
		UsersResource userRessource = keycloak.realm(realm).users();
		List<UserRepresentation> users = userRessource.search(vo.getUserName());
		if(users != null && users.size() > 0) {
			CredentialRepresentation credentail = new CredentialRepresentation();
			credentail.setType(CredentialRepresentation.PASSWORD);
			credentail.setValue(vo.getPassword());
			credentail.setTemporary(vo.isChangedAfterLogin());
			userRessource.get(users.get(0).getId()).resetPassword(credentail);
		}else {
			throw new ZcpException("E00003"); 
		}
	}
	
	public void initUserPassword(Map<String, Object> vo) throws ZcpException{
		UsersResource usersResource = keycloak.realm(realm).users();
		List<UserRepresentation> users = usersResource.search(vo.get("userName").toString());
		UserResource user = usersResource.get(users.get(0).getId());
		if(vo.get("actionType") != null) {
//			CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
//			credentialRepresentation.setDigits(1);
//			credentialRepresentation.setPeriod(Hours);
//			user.resetPassword(credentialRepresentation);
			user.executeActionsEmail("test1", null, (List<String>)vo.get("actionType"));
		}
	}
	
	public void removeOtpPassword(String userName) {
		UsersResource usersResource = keycloak.realm(realm).users();
		List<UserRepresentation> users = usersResource.search(userName);
		UserResource user = usersResource.get(users.get(0).getId());
		user.removeTotp();
	}
	
}
