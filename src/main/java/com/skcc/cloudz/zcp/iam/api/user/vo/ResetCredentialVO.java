package com.skcc.cloudz.zcp.iam.api.user.vo;

import java.util.ArrayList;
import java.util.List;

import com.skcc.cloudz.zcp.iam.common.model.CredentialActionType;

public class ResetCredentialVO {
	private TimeType type;
	private int period;
	private List<CredentialActionType> actions;

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
		this.period = period;
	}

	public List<String> getActions() {
		if (actions == null)
			return null;

		List<String> action = new ArrayList<String>();
		for (CredentialActionType type : actions) {
			action.add(type.name());
		}
		return action;
	}

	public void setActions(List<CredentialActionType> actions) {
		this.actions = actions;
	}

	public enum TimeType {
		MIN, HOUR, DAY;
	}

}
