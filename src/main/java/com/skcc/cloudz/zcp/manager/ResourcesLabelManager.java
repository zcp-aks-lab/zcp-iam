package com.skcc.cloudz.zcp.manager;

import java.util.HashMap;
import java.util.Map;

public class ResourcesLabelManager {
	public static final String SYSTEM_LABEL_NAME = "cloudzcp.io/zcp-system";
	public static final String SYSTEM_LABEL_VALUE = "true";
	public static final String SYSTEM_USER_LABEL_NAME = "cloudzcp.io/zcp-system-user";
	public static final String SYSTEM_USER_LABEL_VALUE = "true";
	public static final String SYSTEM_USERNAME_LABEL_NAME = "cloudzcp.io/zcp-system-username";
	public static final String SYSTEM_NAMESPACE_LABEL_NAME = "cloudzcp.io/zcp-system-namespace";
	
	public static String getSystemLabelSelector() {
		StringBuilder builder = new StringBuilder();
		builder.append(SYSTEM_LABEL_NAME).append("=").append(SYSTEM_LABEL_VALUE);
		return builder.toString() ;
	}
	
	public static String getSystemUserLabelSelector() {
		StringBuilder builder = new StringBuilder();
		builder.append(SYSTEM_LABEL_NAME).append("=").append(SYSTEM_LABEL_VALUE).append(",");
		builder.append(SYSTEM_USER_LABEL_NAME).append("=").append(SYSTEM_USER_LABEL_VALUE);
		return builder.toString() ;
	}
	
	public static String getSystemUsernameLabelSelector(String username) {
		StringBuilder builder = new StringBuilder();
		builder.append(SYSTEM_LABEL_NAME).append("=").append(SYSTEM_LABEL_VALUE).append(",");
		builder.append(SYSTEM_USER_LABEL_NAME).append("=").append(SYSTEM_USER_LABEL_VALUE).append(",");
		builder.append(SYSTEM_USERNAME_LABEL_NAME).append("=").append(username);
		return builder.toString() ;
	}

	public static Map<String, String> getSystemLabels() {
		Map<String, String> labels = new HashMap<String, String>();
		labels.put(SYSTEM_LABEL_NAME, SYSTEM_LABEL_VALUE);
		
		return labels;
	}

	public static Map<String, String> getSystemUserLabels() {
		Map<String, String> labels = new HashMap<String, String>();
		labels.put(SYSTEM_LABEL_NAME, SYSTEM_LABEL_VALUE);
		labels.put(SYSTEM_USER_LABEL_NAME, SYSTEM_USER_LABEL_VALUE);
		
		return labels;
	}

	public static Map<String, String> getSystemUsernameLabels(String username) {
		Map<String, String> labels = new HashMap<String, String>();
		labels.put(SYSTEM_LABEL_NAME, SYSTEM_LABEL_VALUE);
		labels.put(SYSTEM_USER_LABEL_NAME, SYSTEM_USER_LABEL_VALUE);
		labels.put(SYSTEM_USERNAME_LABEL_NAME, username);
		
		return labels;
	}

	public static Map<String, String> getSystemNamespaceLabels(String namespace) {
		Map<String, String> labels = new HashMap<String, String>();
		labels.put(SYSTEM_LABEL_NAME, SYSTEM_LABEL_VALUE);
		labels.put(SYSTEM_NAMESPACE_LABEL_NAME, namespace);
		
		return labels;
	}
}
