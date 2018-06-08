package com.skcc.cloudz.zcp.user.vo;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ClusterRole {
	NONE("none",0),
	VIEW("view",1),
	EDIT("edit",2),
	ADMIN("admin",3),
	CLUSTER_ADMIN("cluster-admin",4);
	
	String role;
	int value;
	
	private ClusterRole(String role, int value) {
		this.role = role;
		this.value = value;
	}
	
	@JsonValue
	public Map<Integer, String> toJson(){
		Map<Integer, String> map = new HashMap<Integer, String>();
		map.put(this.value, this.role);
		return map;
	}
	
	public static int getNumber(String role) {
		int rtn = -1;
		switch(role) {
			case "none" : rtn =0;break;
			case "view" : rtn =1;break;
			case "edit" : rtn =2;break;
			case "admin" : rtn=3;break;
			case "cluster-admin" : rtn=4;break;
		}
		return rtn;
	}
	
	public static String get(int num) {
		String rtn = "";
		switch(num) {
			case 0 : rtn = "none";break;
			case 1 : rtn = "view";break;
			case 2 : rtn = "edit";break;
			case 3 : rtn = "admin";break;
			case 4 : rtn = "cluster-admin";break;
		}
		return rtn ;
	}
	
	public String toString() {
		return this.role;
	}
}
