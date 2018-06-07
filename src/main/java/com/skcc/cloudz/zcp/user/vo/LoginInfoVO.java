package com.skcc.cloudz.zcp.user.vo;

import io.kubernetes.client.models.V1ClusterRoleBinding;
import io.kubernetes.client.models.V1Namespace;

public class LoginInfoVO {
	MemberVO user;
	V1Namespace namespace;
	V1ClusterRoleBinding clusterrolebinding;
	
	
	public V1Namespace getNamespace() {
		return namespace;
	}
	public void setNamespace(V1Namespace namespace) {
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
