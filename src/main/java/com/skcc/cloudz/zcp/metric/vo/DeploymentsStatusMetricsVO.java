package com.skcc.cloudz.zcp.metric.vo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.skcc.cloudz.zcp.common.model.DeploymentStatus;
import com.skcc.cloudz.zcp.common.model.DeploymentStatusMetric;

public class DeploymentsStatusMetricsVO {
	private List<DeploymentStatusMetric> statuses;
	private Integer totalCount;
	private Integer availablePercentage = -1;

	public List<DeploymentStatusMetric> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<DeploymentStatusMetric> statuses) {
		this.statuses = statuses;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public Integer getAvailablePercentage() {
		if (availablePercentage < 0) {
			if (statuses == null)
				return availablePercentage;
			if (totalCount == 0)
				return availablePercentage;

			Map<DeploymentStatus, Integer> map = statuses.stream()
					.collect(Collectors.toMap(DeploymentStatusMetric::getStatus, DeploymentStatusMetric::getCount));
			int availableCount = map.get(DeploymentStatus.Available).intValue();
			return (int) (availableCount / totalCount) * 100;
		}
		return availablePercentage;
	}

	public void setAvailablePercentage(Integer availablePercentage) {
		this.availablePercentage = availablePercentage;
	}

}
