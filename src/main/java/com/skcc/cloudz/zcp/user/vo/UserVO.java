package com.skcc.cloudz.zcp.user.vo;

/**
 * 사용자 리스트를 위한 정보 memberVO와 다름
 * @author Administrator
 *
 */
public class UserVO {
	String userId;
	String email;
	String name;
	String clusterRole;
	int usedNamespace;
	String date;
	boolean status;
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getClusterRole() {
		return clusterRole;
	}
	public void setClusterRole(String clusterRole) {
		this.clusterRole = clusterRole;
	}
	public int getUsedNamespace() {
		return usedNamespace;
	}
	public void setUsedNamespace(int usedNamespace) {
		this.usedNamespace = usedNamespace;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	
	
	
}
