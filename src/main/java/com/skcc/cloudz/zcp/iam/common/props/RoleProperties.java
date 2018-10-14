package com.skcc.cloudz.zcp.iam.common.props;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;

@Component
@ConfigurationProperties(prefix = "role", ignoreUnknownFields = true)
public class RoleProperties {
	public final List<String> NONE = ImmutableList.of();
	/**
	 * Cluster 단위의 Role Mapping.
	 * (Cluster-Role, Keycloak-Realm-Role)
	 */
	private Map<String, List<String>> cluster;

	/**
	 * Namespace 단위의 Role Mapping.
	 * (Cluster-Role, Keycloak-Realm-Role)
	 */
	private Map<String, List<String>> namespace;

	/**
	 * Jenkins 폴더의 config 생성을 위한 Role Mapping.
	 * 템플릿 XML 파일에서 변수로 활용된다.
	 */
	private Map<String, String> jenkins;
	
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
	
	public Map<String, String> getJenkins() {
		return jenkins;
	}
	
	public void setJenkins(Map<String, String> jenkins) {
		this.jenkins = jenkins;
	}
	
	/*
	 * util
	 */
	public List<String> getClusterUserRoles(ClusterRole role){
		return getClusterUserRoles(role.toString());
	}

	public List<String> getClusterUserRoles(String role){
		return this.cluster.get(role);
	}

	public List<String> getNamspaceUserRoles(ClusterRole role, final String namespace){
		return getNamspaceUserRoles(role.toString(), namespace);
	}

	public List<String> getNamspaceUserRoles(String role, final String namespace){
		//TODO: roles if null
		List<String> roles = this.namespace.get(role);
		if(roles == null)
			return NONE;
			//throw new IllegalArgumentException("There is no matched role mappings with '" + role + "'");
		
		return roles.stream()
			.map(name -> {
				return StringSubstitutor.replace(name, ImmutableMap.of("namespace", namespace));
			})
			.collect(Collectors.toList());
	}
}
