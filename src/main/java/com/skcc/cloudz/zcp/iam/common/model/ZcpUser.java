package com.skcc.cloudz.zcp.iam.common.model;

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
	private Boolean enabled;
	private Boolean emailVerified;
	private Boolean totp;
	private ClusterRole clusterRole;
	private List<String> namespaces;
	private String defaultNamespace;
	private int usedNamespace;
	private List<CredentialActionType> requiredActions;
	private ClusterRole namespacedRole;
	private Boolean zdbAdmin;

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

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean getEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(Boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public Boolean getTotp() {
		return totp;
	}

	public void setTotp(Boolean totp) {
		this.totp = totp;
	}

	public List<CredentialActionType> getRequiredActions() {
		return requiredActions;
	}

	public void setRequiredActions(List<CredentialActionType> requiredActions) {
		this.requiredActions = requiredActions;
	}

	public ClusterRole getNamespacedRole() {
		return namespacedRole;
	}

	public void setNamespacedRole(ClusterRole namespacedRole) {
		this.namespacedRole = namespacedRole;
	}
	
    public Boolean getZdbAdmin() {
        return zdbAdmin;
    }

    public void setZdbAdmin(Boolean zdbAdmin) {
        this.zdbAdmin = zdbAdmin;
    }

    @Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ZcpUser [id=");
		builder.append(id);
		builder.append(", username=");
		builder.append(username);
		builder.append(", email=");
		builder.append(email);
		builder.append(", password=");
		builder.append(password);
		builder.append(", firstName=");
		builder.append(firstName);
		builder.append(", lastName=");
		builder.append(lastName);
		builder.append(", createdDate=");
		builder.append(createdDate);
		builder.append(", enabled=");
		builder.append(enabled);
		builder.append(", emailVerified=");
		builder.append(emailVerified);
		builder.append(", totp=");
		builder.append(totp);
		builder.append(", clusterRole=");
		builder.append(clusterRole);
		builder.append(", namespaces=");
		builder.append(namespaces);
		builder.append(", defaultNamespace=");
		builder.append(defaultNamespace);
		builder.append(", usedNamespace=");
		builder.append(usedNamespace);
		builder.append(", requiredActions=");
		builder.append(requiredActions);
		builder.append(", namespacedRole=");
		builder.append(namespacedRole);
		builder.append(", zdbAdmin=");
        builder.append(zdbAdmin);
		builder.append("]");
		return builder.toString();
	}

}
