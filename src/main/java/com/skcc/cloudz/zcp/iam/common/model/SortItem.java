package com.skcc.cloudz.zcp.iam.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SortItem {
	NAMESPACE("namespace", 0)
	, CPU("cpu", 1)
	, MEMORY("memory", 2)
	, USER("user", 3)
	, STATUS("status", 4)
	, CREATE_TIME("createTime", 5);

	private String item;
	private int value;

	private SortItem(String item, int value) {
		this.item = item;
		this.value = value;
	}
	
	class SimpleItem {
		private String name;
		private int value;

		public SimpleItem(String name, int value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return this.name;
		}

		public int getValue() {
			return this.value;
		}
	}
	
	@JsonValue
	public SimpleItem jsonValue() {
		return new SimpleItem(this.item, this.value);
	}
	
	@JsonCreator
	public static SortItem getSortItem(String item) {
		for (SortItem s : values()) {
			if (s.getItem().equals(item)) {
				return s;
			}
		}

		return null;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	
}
