package com.skcc.cloudz.zcp.iam.api.user.vo;

import javax.validation.constraints.NotNull;

import com.skcc.cloudz.zcp.iam.common.vo.Ivo;

public class UpdatePasswordVO implements Ivo {
	@NotNull
	private String currentPassword;
	@NotNull
	private String newPassword;

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}


}
