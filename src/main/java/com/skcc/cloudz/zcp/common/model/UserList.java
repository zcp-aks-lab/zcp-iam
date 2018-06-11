package com.skcc.cloudz.zcp.common.model;

import java.util.List;

public class UserList {
	List<ZcpUser> items;
	
	public UserList() {
		super();
	}
	
	public UserList(List<ZcpUser> items) {
		this.items = items;
	}

	public List<ZcpUser> getItems() {
		return items;
	}

	public void setItems(List<ZcpUser> items) {
		this.items = items;
	}

}
