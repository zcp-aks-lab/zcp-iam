package com.skcc.cloudz.zcp.iam.api.namespace.vo;

import java.util.List;

public class ItemList<T> {
	List<T> items;

	public List<T> getItems() {
		return items;
	}

	public void setItems(List<T> items) {
		this.items = items;
	}

}
