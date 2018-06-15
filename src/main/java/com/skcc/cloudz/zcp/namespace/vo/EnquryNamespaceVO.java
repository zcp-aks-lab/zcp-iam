package com.skcc.cloudz.zcp.namespace.vo;

public class EnquryNamespaceVO {
	String namespace;
	String label;
	String sortItem;
	boolean sortOrder;
	
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getSortItem() {
		return sortItem;
	}
	public void setSortItem(String sortItem) {
		this.sortItem = sortItem;
	}
	public boolean isSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(boolean sortOrder) {
		this.sortOrder = sortOrder;
	}
	
	
}
