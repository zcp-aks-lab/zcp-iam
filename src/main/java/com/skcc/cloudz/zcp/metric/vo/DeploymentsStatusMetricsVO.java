package com.skcc.cloudz.zcp.metric.vo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.skcc.cloudz.zcp.common.model.DeploymentStatus;
import com.skcc.cloudz.zcp.common.model.DeploymentStatusMetric;
import com.skcc.cloudz.zcp.common.util.NumberUtils;

public class DeploymentsStatusMetricsVO {
	private String title = "Deployments";
	private List<DeploymentStatusMetric> statuses;
	private Integer totalCount;
	private String mainStatusPercentage;
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

	public String getMainStatusPercentage() {
		if (StringUtils.isEmpty(mainStatusPercentage)) {
			if (statuses == null)
				return "0";
			if (totalCount == 0)
				return "0";

			Map<DeploymentStatus, Integer> map = statuses.stream()
					.collect(Collectors.toMap(DeploymentStatusMetric::getStatus, DeploymentStatusMetric::getCount));
			double availableCount = map.get(mainStatus).doubleValue();
			
			return NumberUtils.percentFormat(availableCount, totalCount.doubleValue());
		}
		return mainStatusPercentage;
	}

	public void setMainStatusPercentage(String mainStatusPercentage) {
		this.mainStatusPercentage = mainStatusPercentage;
	}

	public DeploymentStatus getMainStatus() {
		return mainStatus;
	}

	public void setMainStatus(DeploymentStatus mainStatus) {
		this.mainStatus = mainStatus;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}


}
