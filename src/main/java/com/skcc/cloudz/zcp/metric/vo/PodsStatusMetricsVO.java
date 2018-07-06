package com.skcc.cloudz.zcp.metric.vo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skcc.cloudz.zcp.common.model.PodStatus;
import com.skcc.cloudz.zcp.common.model.PodStatusMetric;
import com.skcc.cloudz.zcp.common.util.NumberUtils;

public class PodsStatusMetricsVO {
	
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(PodsStatusMetricsVO.class);
	
	private String title = "Pods";
	private List<PodStatusMetric> statuses;
	private Integer totalCount;
	private String mainStatusPercentage;
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

	public String getMainStatusPercentage() {
		if (StringUtils.isEmpty(mainStatusPercentage)) {
			if (statuses == null)
				return "0";
			if (totalCount == 0)
				return "0";

			Map<PodStatus, Integer> map = statuses.stream()
					.collect(Collectors.toMap(PodStatusMetric::getStatus, PodStatusMetric::getCount));
			double availableCount = map.get(mainStatus).doubleValue();
			
			return NumberUtils.percentFormat(availableCount, totalCount.doubleValue());
		}
		return mainStatusPercentage;
	}

	public void setMainStatusPercentage(String mainStatusPercentage) {
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
