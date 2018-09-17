package com.skcc.cloudz.zcp.iam.common.props;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;

@Component
@ConfigurationProperties(prefix = "role", ignoreUnknownFields = true)
public class RoleProperties {
	private Map<String, List<String>> cluster;
	private Map<String, List<String>> namespace;
	
	/*
	 * getter & setter
	 */
	public Map<String, List<String>> getCluster() {
		return cluster;
	}
	
	public void setCluster(Map<String, List<String>> cluster) {
		this.cluster = cluster;
	}
	
	public Map<String, List<String>> getNamespace() {
		return namespace;
	}
	
	public void setNamespace(Map<String, List<String>> namespace) {
		this.namespace = namespace;
	}
	
	/*
	 * util
	 */
	public List<String> getClusterUserRoles(String role){
		return this.cluster.get(role);
	}

	public List<String> getNamspaceUserRoles(ClusterRole role, final String namespace){
		return getNamspaceUserRoles(role.toString(), namespace);
	}

	public List<String> getNamspaceUserRoles(String role, final String namespace){
		//TODO: roles if null
		List<String> roles = this.namespace.get(role);
		return roles.stream()
			.map(name -> {
				return StringSubstitutor.replace(name, ImmutableMap.of("namespace", namespace));
			})
			.collect(Collectors.toList());
	}
}
