package com.skcc.cloudz.zcp.user.vo;

import io.kubernetes.client.models.V1ClusterRoleBinding;
import io.kubernetes.client.models.V1NamespaceList;

public class LoginInfoVO {
	MemberVO user;
	V1NamespaceList namespace;
	V1ClusterRoleBinding clusterrolebinding;
	
	public V1NamespaceList getNamespace() {
		return namespace;
	}
	public void setNamespace(V1NamespaceList namespace) {
		this.namespace = namespace;
	}
	public V1ClusterRoleBinding getClusterrolebinding() {
		return clusterrolebinding;
	}
	public void setClusterrolebinding(V1ClusterRoleBinding clusterrolebinding) {
		this.clusterrolebinding = clusterrolebinding;
	}
	public MemberVO getUser() {
		return user;
	}
	public void setUser(MemberVO user) {
		this.user = user;
	}
	
	
}
