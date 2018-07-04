package com.skcc.cloudz.zcp.metric.vo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.skcc.cloudz.zcp.common.model.DeploymentStatus;
import com.skcc.cloudz.zcp.common.model.DeploymentStatusMetric;

public class DeploymentsStatusMetricsVO {
	private List<DeploymentStatusMetric> statuses;
	private Integer totalCount;
	private Integer mainStatusPercentage = -1;
	private DeploymentStatus mainStatus = DeploymentStatus.Available;
	
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

	public Integer getMainStatusPercentage() {
		if (mainStatusPercentage < 0) {
			if (statuses == null)
				return mainStatusPercentage;
			if (totalCount == 0)
				return mainStatusPercentage;

			Map<DeploymentStatus, Integer> map = statuses.stream()
					.collect(Collectors.toMap(DeploymentStatusMetric::getStatus, DeploymentStatusMetric::getCount));
			float availableCount = map.get(mainStatus).floatValue();
			
			return (int) (availableCount / totalCount.floatValue()) * 100;
		}
		return mainStatusPercentage;
	}

	public void setMainStatusPercentage(Integer mainStatusPercentage) {
		this.mainStatusPercentage = mainStatusPercentage;
	}

	public DeploymentStatus getMainStatus() {
		return mainStatus;
	}

	public void setMainStatus(DeploymentStatus mainStatus) {
		this.mainStatus = mainStatus;
	}


}
