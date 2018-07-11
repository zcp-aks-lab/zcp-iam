package com.skcc.cloudz.zcp.iam.api.namespace.vo;

import javax.validation.constraints.NotNull;

import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;
import com.skcc.cloudz.zcp.iam.common.vo.Ivo;

public class RoleBindingVO implements Ivo {
	@NotNull
	private String username;
	@NotNull
	private ClusterRole clusterRole;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public ClusterRole getClusterRole() {
		return clusterRole;
	}

	public void setClusterRole(ClusterRole clusterRole) {
		this.clusterRole = clusterRole;
	}

}
