package com.skcc.cloudz.zcp.metric.vo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skcc.cloudz.zcp.common.model.PodStatus;
import com.skcc.cloudz.zcp.common.model.PodStatusMetric;

public class PodsStatusMetricsVO {
	
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(PodsStatusMetricsVO.class);
	
	private List<PodStatusMetric> statuses;
	private Integer totalCount;
	private Integer mainStatusPercentage = -1;
	private PodStatus mainStatus = PodStatus.Running;

	public List<PodStatusMetric> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<PodStatusMetric> statuses) {
		this.statuses = statuses;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public Integer getMainStatusPercentage() {
		if (mainStatusPercentage < 0) { // if not set then calculate the pecent automatically
			if (statuses == null)
				return mainStatusPercentage;
			if (totalCount == 0)
				return mainStatusPercentage;

			Map<PodStatus, Integer> map = statuses.stream()
					.collect(Collectors.toMap(PodStatusMetric::getStatus, PodStatusMetric::getCount));
			float availableCount = map.get(mainStatus).floatValue();
			
			return (int) ((availableCount / totalCount.floatValue()) * 100);
		}
		return mainStatusPercentage;
	}

	public void setMainStatusPercentage(Integer mainStatusPercentage) {
		this.mainStatusPercentage = mainStatusPercentage;
	}

	public PodStatus getMainStatus() {
		return mainStatus;
	}

	public void setMainStatus(PodStatus mainStatus) {
		this.mainStatus = mainStatus;
	}

}
