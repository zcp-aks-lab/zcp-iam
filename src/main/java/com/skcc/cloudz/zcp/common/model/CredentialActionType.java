package com.skcc.cloudz.zcp.common.model;

public enum CredentialActionType {
	UPDATE_PASSWORD, 
	UPDATE_PROFILE, 
	VERIFY_EMAIL, 
	CONFIGURE_TOTP;

	public static CredentialActionType getActionType(String name) {
		for (CredentialActionType t : values()) {
			if (t.name().equals(name)) {
				return t;
			}
		}

		throw new IllegalArgumentException("[" + name + "] is invalid");
	}
}
