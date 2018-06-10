package com.skcc.cloudz.zcp.manager;

public class ResourcesNameManager {
	private static final String SERVICE_ACCOUNT_PREFIX = "zcp-system-sa-";
	private static final String CLUSTER_ROLE_BINDING_PREFIX = "zcp-system-crb-";
	private static final String ROLE_BINDING_PREFIX = "zcp-system-rb-";

	public static String getServiceAccountName(String username) {
		StringBuilder builder = new StringBuilder();
		builder.append(SERVICE_ACCOUNT_PREFIX).append(username);
		return builder.toString();
	}
	
	public static String getClusterRoleBindingName(String username) {
		StringBuilder builder = new StringBuilder();
		builder.append(CLUSTER_ROLE_BINDING_PREFIX).append(username);
		return builder.toString();
	}
	
	public static String getRoleBindingName(String username) {
		StringBuilder builder = new StringBuilder();
		builder.append(ROLE_BINDING_PREFIX).append(username);
		return builder.toString();
	}
}
