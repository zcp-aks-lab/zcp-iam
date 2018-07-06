package com.skcc.cloudz.zcp.metric.vo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.skcc.cloudz.zcp.common.model.NodeStatus;
import com.skcc.cloudz.zcp.common.model.NodeStatusMetric;
import com.skcc.cloudz.zcp.common.util.NumberUtils;

public class NodesStatusMetricsVO {
	private String title = "Nodes";
	private List<NodeStatusMetric> statuses;
	private Integer totalCount;
	private String mainStatusPercentage;
	private NodeStatus mainStatus = NodeStatus.Ready;

	public List<NodeStatusMetric> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<NodeStatusMetric> statuses) {
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

			Map<NodeStatus, Integer> map = statuses.stream()
					.collect(Collectors.toMap(NodeStatusMetric::getStatus, NodeStatusMetric::getCount));
			double availableCount = map.get(mainStatus).doubleValue();
			
			return NumberUtils.percentFormat(availableCount, totalCount.doubleValue());
		}
		return mainStatusPercentage;
	}

	public void setMainStatusPercentage(String mainStatusPercentage) {
		this.mainStatusPercentage = mainStatusPercentage;
	}

	public NodeStatus getMainStatus() {
		return mainStatus;
	}

	public void setMainStatus(NodeStatus mainStatus) {
		this.mainStatus = mainStatus;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
