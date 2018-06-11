package com.skcc.cloudz.zcp.user.vo;

import javax.validation.constraints.NotNull;

import com.skcc.cloudz.zcp.common.model.ClusterRole;
import com.skcc.cloudz.zcp.common.vo.Ivo;

public class UpdateClusterRoleVO implements Ivo {
	@NotNull
	private String id;
	@NotNull
	private ClusterRole clusterRole;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ClusterRole getClusterRole() {
		return clusterRole;
	}

	public void setClusterRole(ClusterRole clusterRole) {
		this.clusterRole = clusterRole;
	}

}
