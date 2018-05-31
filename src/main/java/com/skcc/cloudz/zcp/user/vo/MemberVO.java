package com.skcc.cloudz.zcp.user.vo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.skcc.cloudz.zcp.common.vo.Ivo;

public class MemberVO implements Ivo {
	String userName;//사용자 ID
	String firstName;
	String lastName;
	String email;
	String password;
	ClusterRole clusterRole;
	Attribute attribute = new Attribute();
	boolean enabled;
	boolean isChangedAfterLogin;
	
	public ClusterRole getClusterRole() {
		return clusterRole;
	}
	public void setClusterRole(ClusterRole clusterRole) {
		this.clusterRole = clusterRole;
	}
	public boolean isChangedAfterLogin() {
		return isChangedAfterLogin;
	}
	public void setChangedAfterLogin(boolean isChangedAfterLogin) {
		this.isChangedAfterLogin = isChangedAfterLogin;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean getEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public Attribute getAttribute() {
		return attribute;
	}
	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}
	
	public void setAttributeMap(Map<String, List<String>> attr) {
		if(attr != null) {
			this.attribute.setDefaultNamespace(attr.get("defaultNamespace"));
			this.attribute.setEmailVerified(attr.get("emailVerified"));
		}
	}
	
	public Map<String, List<String>> getAttributeMap() {
		Map<String, List<String>> attr = new HashMap ();
		
		attr.put("defaultNamespace", this.attribute.getDefaultNamespace());
		attr.put("emailVerified", this.attribute.getEmailVerified());
		
		return attr;
		
	}

	public class Attribute implements Ivo {
		List<String> defaultNamespace;
		List<String> emailVerified;
		
		public List<String> getDefaultNamespace() {
			return defaultNamespace;
		}
		public void setDefaultNamespace(List<String> defaultNamespace) {
			this.defaultNamespace = defaultNamespace;
		}
		public List<String> getEmailVerified() {
			return emailVerified;
		}
		public void setEmailVerified(List<String> emailVerified) {
			this.emailVerified = emailVerified;
		}
		public void asListEmailVerified(Boolean emailVerified2) {
			String[] bool = {emailVerified2 ? "true" : "false"};
			this.emailVerified = Arrays.asList(bool);
		}
	}
}


