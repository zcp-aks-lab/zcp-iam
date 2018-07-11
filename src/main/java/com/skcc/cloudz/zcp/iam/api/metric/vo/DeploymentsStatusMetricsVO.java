package com.skcc.cloudz.zcp.iam.api.metric.vo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.skcc.cloudz.zcp.iam.common.model.DeploymentStatus;
import com.skcc.cloudz.zcp.iam.common.model.DeploymentStatusMetric;
import com.skcc.cloudz.zcp.iam.common.util.NumberUtils;

public class DeploymentsStatusMetricsVO {
	private String title = "Deployments";
	private List<DeploymentStatusMetric> statuses;
	private BigDecimal totalCount;
	private BigDecimal mainStatusPercentage;
	private DeploymentStatus mainStatus = DeploymentStatus.Available;

	public List<DeploymentStatusMetric> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<DeploymentStatusMetric> statuses) {
		this.statuses = statuses;
	}

	public BigDecimal getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(BigDecimal totalCount) {
		this.totalCount = totalCount;
	}

	public BigDecimal getMainStatusPercentage() {
		if (mainStatusPercentage == null) {
			if (statuses == null)
				return BigDecimal.valueOf(0);
			if (totalCount == null)
				return BigDecimal.valueOf(0);

			Map<DeploymentStatus, Integer> map = statuses.stream()
					.collect(Collectors.toMap(DeploymentStatusMetric::getStatus, DeploymentStatusMetric::getCount));
			double availableCount = map.get(mainStatus).doubleValue();

			return NumberUtils.percent(availableCount, totalCount.doubleValue());
		}
		return mainStatusPercentage;
	}

	public void setMainStatusPercentage(BigDecimal mainStatusPercentage) {
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
