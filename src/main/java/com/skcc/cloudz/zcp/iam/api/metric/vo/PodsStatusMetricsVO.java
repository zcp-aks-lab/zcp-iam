package com.skcc.cloudz.zcp.iam.api.metric.vo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skcc.cloudz.zcp.iam.common.model.PodStatus;
import com.skcc.cloudz.zcp.iam.common.model.PodStatusMetric;
import com.skcc.cloudz.zcp.iam.common.util.NumberUtils;

public class PodsStatusMetricsVO {

	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(PodsStatusMetricsVO.class);

	private String title = "Pods";
	private List<PodStatusMetric> statuses;
	private BigDecimal totalCount;
	private BigDecimal mainStatusPercentage;
	private PodStatus mainStatus = PodStatus.Running;

	public List<PodStatusMetric> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<PodStatusMetric> statuses) {
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

			Map<PodStatus, Integer> map = statuses.stream()
					.collect(Collectors.toMap(PodStatusMetric::getStatus, PodStatusMetric::getCount));
			double availableCount = map.get(mainStatus).doubleValue();

			return NumberUtils.percent(availableCount, totalCount.doubleValue());
		}
		return mainStatusPercentage;
	}

	public void setMainStatusPercentage(BigDecimal mainStatusPercentage) {
		this.mainStatusPercentage = mainStatusPercentage;
	}

	public PodStatus getMainStatus() {
		return mainStatus;
	}

	public void setMainStatus(PodStatus mainStatus) {
		this.mainStatus = mainStatus;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
