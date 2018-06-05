package com.skcc.cloudz.zcp.user.dao;

import java.util.List;

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
import com.skcc.cloudz.zcp.user.vo.PassResetVO;

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
		UserRepresentation user = getUser(userName);
		if(user != null) {
			userRessource.get(user.getId()).remove();
		}
	}
	
	public void editAttribute(MemberVO vo) throws ZcpException{
		UsersResource userRessource = keycloak.realm(realm).users();
		UserRepresentation user = getUser(vo.getUserName());
		if(user != null) {
			userRessource.get(user.getId()).update(user);
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
		UserRepresentation user = getUser(vo.getUserName());
		if(user != null) {
			user.setFirstName(vo.getFirstName());
			user.setLastName(vo.getLastName());
			user.setEmail(vo.getEmail());
			user.setAttributes(vo.rcvAttributeMap());
			user.setUsername(vo.getUserName());
			user.setEnabled(vo.getEnabled());
			userRessource.get(user.getId()).update(user);
		}
	}
	
	public MemberVO getUser(MemberVO vo) throws ZcpException{
		UserRepresentation userp = getUser(vo.getUserName());
		if(userp != null) {
				vo.setFirstName(userp.getFirstName());
				vo.putAttributeMap(userp.getAttributes());
				vo.setEmail(userp.getEmail());
				vo.setEnabled(userp.isEnabled());
				vo.getAttribute().asListEmailVerified(userp.isEmailVerified());
				
				return vo; 
		}else
			return vo;
	}
	
	
	
	private UserRepresentation getUser(String username) throws ZcpException {
		UsersResource userRessource = keycloak.realm(realm).users();
		List<UserRepresentation> users = userRessource.search(username);
		UserRepresentation user=null;
		for(UserRepresentation userp : users) {
			if(users != null && users.size() > 0 && userp.getUsername().equals(username)) {
				user=userp;
			}
		}
		if(user ==  null) {
			LOG.debug("User name didn't find");
			throw new ZcpException("E00003"); 
		}else
			return user;
	}
	
	
	public void editUserPassword(MemberVO vo) throws ZcpException{
		UsersResource userRessource = keycloak.realm(realm).users();
		UserRepresentation userp = getUser(vo.getUserName());
		if(userp != null) {
			CredentialRepresentation credentail = new CredentialRepresentation();
			credentail.setType(CredentialRepresentation.PASSWORD);
			credentail.setValue(vo.getPassword());
			credentail.setTemporary(vo.isChangedAfterLogin());
			userRessource.get(userp.getId()).resetPassword(credentail);
		}
	}
	
	public void initUserPassword(PassResetVO vo) throws ZcpException{
		UsersResource usersResource = keycloak.realm(realm).users();
		UserRepresentation userp = getUser(vo.getUserName());
		if(userp != null) {
			UserResource user = usersResource.get(userp.getId());
//			CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
//			credentialRepresentation.setPeriod(vo.getPeriod());
//			//usersResource.get(userp.getId()).update(userp);
//			credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
//			user.resetPassword(credentialRepresentation);
			user.executeActionsEmail(null, vo.getRedirectUri(), vo.getActions());
		}
	}
	
	public void removeOtpPassword(String userName) throws ZcpException {
		UsersResource usersResource = keycloak.realm(realm).users();
		UserRepresentation userp = getUser(userName);
		if(userp != null) {
			UserResource user = usersResource.get(userp.getId());
			user.removeTotp();
		}
	}
	
}
