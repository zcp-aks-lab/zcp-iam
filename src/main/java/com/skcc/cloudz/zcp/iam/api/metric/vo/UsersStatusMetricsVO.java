package com.skcc.cloudz.zcp.iam.api.metric.vo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skcc.cloudz.zcp.iam.common.model.ClusterRole;
import com.skcc.cloudz.zcp.iam.common.model.UserStatusMetric;
import com.skcc.cloudz.zcp.iam.common.util.NumberUtils;

public class UsersStatusMetricsVO {

	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(UsersStatusMetricsVO.class);

	private String title = "Users";
	private List<UserStatusMetric> roles;
	private BigDecimal totalCount;
	private BigDecimal mainRolePercentage;
	private ClusterRole mainRole = ClusterRole.ADMIN;

	public List<UserStatusMetric> getRoles() {
		return roles;
	}

	public void setRoles(List<UserStatusMetric> roles) {
		this.roles = roles;
	}

	public BigDecimal getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(BigDecimal totalCount) {
		this.totalCount = totalCount;
	}

	public BigDecimal getMainRolePercentage() {
		if (mainRolePercentage == null) {
			if (roles == null)
				return BigDecimal.valueOf(0);
			if (totalCount == null) 
				return BigDecimal.valueOf(0);

			Map<ClusterRole, Integer> map = roles.stream()
					.collect(Collectors.toMap(UserStatusMetric::getRole, UserStatusMetric::getCount));
			double availableCount = map.get(mainRole).doubleValue();

			return NumberUtils.percent(availableCount, totalCount.doubleValue());
		}
		return mainRolePercentage;
	}

	public void setMainRolePercentage(BigDecimal mainRolePercentage) {
		this.mainRolePercentage = mainRolePercentage;
	}

	public ClusterRole getMainRole() {
		return mainRole;
	}

	public void setMainRole(ClusterRole mainRole) {
		this.mainRole = mainRole;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
