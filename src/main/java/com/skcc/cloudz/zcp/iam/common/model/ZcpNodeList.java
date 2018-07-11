package com.skcc.cloudz.zcp.iam.common.model;

import java.util.List;

public class ZcpNodeList {
	List<ZcpNode> items;
	
	public ZcpNodeList() {
		super();
	}
	
	public ZcpNodeList(List<ZcpNode> items) {
		this.items = items;
	}

	public List<ZcpNode> getItems() {
		return items;
	}

	public void setItems(List<ZcpNode> items) {
		this.items = items;
	}
}
