package com.skcc.cloudz.zcp.iam.api.user.vo;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;
import com.skcc.cloudz.zcp.iam.common.model.CredentialActionType;
import com.skcc.cloudz.zcp.iam.common.vo.Ivo;

public class MemberVO implements Ivo {
	@NotNull
	private String username;
	@NotNull
	private String firstName;
	private String lastName;
	@NotNull
	private String email;
	private String defaultNamespace;
	private ClusterRole clusterRole = ClusterRole.NONE;
	private Boolean enabled;
	private List<CredentialActionType> requiredActions;
	private Boolean emailVerified;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public ClusterRole getClusterRole() {
		return clusterRole;
	}

	public void setClusterRole(ClusterRole clusterRole) {
		this.clusterRole = clusterRole;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getDefaultNamespace() {
		return defaultNamespace;
	}

	public void setDefaultNamespace(String defaultNamespace) {
		this.defaultNamespace = defaultNamespace;
	}

	public List<CredentialActionType> getRequiredActions() {
		return requiredActions;
	}

	public void setRequiredActions(List<CredentialActionType> requiredActions) {
		this.requiredActions = requiredActions;
	}

	public Boolean getEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(Boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

}
