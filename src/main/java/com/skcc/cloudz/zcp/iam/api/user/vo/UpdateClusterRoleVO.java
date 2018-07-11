package com.skcc.cloudz.zcp.iam.api.user.vo;

import javax.validation.constraints.NotNull;

import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;
import com.skcc.cloudz.zcp.iam.common.vo.Ivo;

public class UpdateClusterRoleVO implements Ivo {
	@NotNull
	private ClusterRole clusterRole;

	public ClusterRole getClusterRole() {
		return clusterRole;
	}

	public void setClusterRole(ClusterRole clusterRole) {
		this.clusterRole = clusterRole;
	}

}
