package com.skcc.cloudz.zcp.iam.manager;

public class ResourcesNameManager {
	// ~~~~~~~~ for user resources
	private static final String SERVICE_ACCOUNT_PREFIX = "zcp-system-sa-";
	private static final String CLUSTER_ROLE_BINDING_PREFIX = "zcp-system-crb-";
	private static final String ROLE_BINDING_PREFIX = "zcp-system-rb-";
	
	// ~~~~~~~~ for namespace resources
	private static final String RESOURCE_QUOTA_PREFIX = "zcp-system-rq-";
	private static final String LIMIT_RANGE_PREFIX = "zcp-system-lr-";

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
	
	public static String getResouceQuotaName(String namespace) {
		StringBuilder builder = new StringBuilder();
		builder.append(RESOURCE_QUOTA_PREFIX).append(namespace);
		return builder.toString();
	}

	public static String getLimtRangeName(String namespace) {
		StringBuilder builder = new StringBuilder();
		builder.append(LIMIT_RANGE_PREFIX).append(namespace);
		return builder.toString();
	}


}
