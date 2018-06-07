package com.skcc.cloudz.zcp.user.vo;

import java.util.ArrayList;
import java.util.List;

public class PassResetVO {
	String userName;
	TimeType type;
	int period;
	String redirectUri;
	List<ActionType> actions;
	
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public TimeType getType() {
		return type;
	}
	public void setType(TimeType type) {
		this.type = type;
	}
	public int getPeriod() {
		return period;
	}
	public void setPeriod(int period) {
		switch(type) {
			case DAY : period = period * 86400; break;
			case HOUR : period = period * 3600; break;
			case MIN : period = period * 60 ;break;
			default : period = 0;
		}
	}
	
	
	public String getRedirectUri() {
		return redirectUri;
	}
	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}
	public List<String> getActions() {
		List<String> action = new ArrayList<String>();
		for(ActionType type : actions) {
			action.add(type.name());	
		}
		return action;
	}
	public void setActions(List<ActionType> actions) {
		this.actions = actions;
	}


	public enum TimeType {
		MIN("MIN"),
		HOUR("HOUR"),
		DAY("DAY");
		
		String type;
		
		private TimeType(String type) {
			this.type = type;
		}
	}
	
	public enum ActionType {
		UPDATE_PASSWORD("UPDATE_PASSWORD"),
		UPDATE_PROFILE("HOUR"),
		VERIFY_EMAIL("DAY"),
		CONFIGURE_OTP("CONFIGURE_OTP");
		
		String type;
		
		private ActionType(String type) {
			this.type = type;
		}
	}
	
}
