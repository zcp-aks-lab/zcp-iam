package com.skcc.cloudz.zcp.member.vo;

import java.util.Map;

public class MemberVO {
	String userName;//사용자 ID
	String firstName;
	String lastName;
	String email;
	Map attribute;
	boolean enabled;
	
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
	public Map getAttribute() {
		return attribute;
	}
	public void setAttribute(Map attribute) {
		this.attribute = attribute;
	}
	
	
}

