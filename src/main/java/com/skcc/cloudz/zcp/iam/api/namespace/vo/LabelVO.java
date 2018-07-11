package com.skcc.cloudz.zcp.iam.api.namespace.vo;

import javax.validation.constraints.NotNull;

public class LabelVO {
	@NotNull
	private String label;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
