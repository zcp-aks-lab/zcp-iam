package com.skcc.cloudz.zcp.user.vo;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

public class ZcpUser {
	private String id;
	@NotNull
	private String username;
	@NotNull
	private String email;
	@NotNull
	private String password;
	@NotNull
	private String firstName;
	private String lastName;
	private Date createdDate;
	private boolean enabled;
	private boolean emailVerified;
	private ClusterRole clusterRole;
	private List<String> namespaces;
	private String defaultNamespace;
	private int usedNamespace;

	public ZcpUser() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public ClusterRole getClusterRole() {
		return clusterRole;
	}

	public void setClusterRole(ClusterRole clusterRole) {
		this.clusterRole = clusterRole;
	}

	public List<String> getNamespaces() {
		return namespaces;
	}

	public void setNamespaces(List<String> namespaces) {
		this.namespaces = namespaces;
	}

	public String getDefaultNamespace() {
		return defaultNamespace;
	}

	public void setDefaultNamespace(String defaultNamespace) {
		this.defaultNamespace = defaultNamespace;
	}

	public int getUsedNamespace() {
		return usedNamespace;
	}

	public void setUsedNamespace(int usedNamespace) {
		this.usedNamespace = usedNamespace;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
