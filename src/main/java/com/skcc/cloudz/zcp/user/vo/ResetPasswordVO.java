package com.skcc.cloudz.zcp.user.vo;

import javax.validation.constraints.NotNull;

import com.skcc.cloudz.zcp.common.vo.Ivo;

public class ResetPasswordVO implements Ivo {
	@NotNull
	private String password;
	private Boolean temporary = Boolean.FALSE;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getTemporary() {
		return temporary;
	}

	public void setTemporary(Boolean temporary) {
		this.temporary = temporary;
	}

}
