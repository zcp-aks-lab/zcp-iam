package com.skcc.cloudz.zcp.member.dao;

import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.skcc.cloudz.zcp.member.vo.MemberVO;

@Component
public class MemberKeycloakDao {

	private static final Logger LOG =  LoggerFactory.getLogger(MemberKeycloakDao.class);    
	   
	@Autowired
	@Qualifier("keycloak")
	Keycloak keycloak;
	
	@Value("${zcp.realm}")
	String realm;
	
	public List<UserRepresentation> getUserList(){
		RealmResource realmResource = keycloak.realm(realm);
		return realmResource.users().list();
	}


	public void modifyUserAttribute(MemberVO vo){
		UsersResource userRessource = keycloak.realm(realm).users();
		UserRepresentation user = userRessource.search(vo.getUserName()).get(0);
		user.setAttributes(vo.getAttribute());
		userRessource.get(user.getId()).update(user);
	}
	
	
	public void deleteUser(MemberVO vo){
		UsersResource userRessource = keycloak.realm(realm).users();
		UserRepresentation user = userRessource.search(vo.getUserName()).get(0);
		userRessource.get(user.getId()).remove();
	}
	
	
	public void createUser(MemberVO vo){
		UsersResource userRessource = keycloak.realm(realm).users();
		UserRepresentation user = new UserRepresentation();
		user.setFirstName(vo.getFirstName());
		user.setLastName(vo.getLastName());
		user.setEmail(vo.getEmail());
		user.setAttributes(vo.getAttribute());
		user.setUsername(vo.getUserName());
		userRessource.create(user);
	}

	
}
