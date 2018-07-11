package com.skcc.cloudz.zcp.iam.common.model;

import java.util.List;

public class ZcpUserList {
	List<ZcpUser> items;
	
	public ZcpUserList() {
		super();
	}
	
	public ZcpUserList(List<ZcpUser> items) {
		this.items = items;
	}

	public List<ZcpUser> getItems() {
		return items;
	}

	public void setItems(List<ZcpUser> items) {
		this.items = items;
	}

}
