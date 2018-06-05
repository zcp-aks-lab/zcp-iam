package com.skcc.cloudz.zcp.user.vo;

import java.util.List;

public class PassResetVO {
	String userName;
	TimeType type;
	int period;
	String redirect_uri;
	List<String> actions;
	
	
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
	public String getRedirect_uri() {
		return redirect_uri;
	}
	public void setRedirect_uri(String redirect_uri) {
		this.redirect_uri = redirect_uri;
	}
	public List<String> getActions() {
		return actions;
	}
	public void setActions(List<String> actions) {
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
	
}
