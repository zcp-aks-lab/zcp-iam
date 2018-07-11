package com.skcc.cloudz.zcp.iam.common.model;

import java.util.List;

public class ZcpNamespaceList {
	List<ZcpNamespace> items;
	
	public ZcpNamespaceList() {
		super();
	}
	
	public ZcpNamespaceList(List<ZcpNamespace> items) {
		this.items = items;
	}

	public List<ZcpNamespace> getItems() {
		return items;
	}

	public void setItems(List<ZcpNamespace> items) {
		this.items = items;
	}
}
