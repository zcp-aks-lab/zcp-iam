package com.skcc.cloudz.zcp.metric.vo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.skcc.cloudz.zcp.common.model.NodeStatus;
import com.skcc.cloudz.zcp.common.model.NodeStatusMetric;

public class NodesStatusMetricsVO {
	private List<NodeStatusMetric> statuses;
	private Integer totalCount;
	private Integer mainStatusPercentage = -1;
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

	public Integer getMainStatusPercentage() {
		if (mainStatusPercentage < 0) {
			if (statuses == null)
				return mainStatusPercentage;
			if (totalCount == 0)
				return mainStatusPercentage;

			Map<NodeStatus, Integer> map = statuses.stream()
					.collect(Collectors.toMap(NodeStatusMetric::getStatus, NodeStatusMetric::getCount));
			float availableCount = map.get(mainStatus).floatValue();

			return (int) (availableCount / totalCount.floatValue()) * 100;
		}
		return mainStatusPercentage;
	}

	public void setMainStatusPercentage(Integer mainStatusPercentage) {
		this.mainStatusPercentage = mainStatusPercentage;
	}

	public NodeStatus getMainStatus() {
		return mainStatus;
	}

	public void setMainStatus(NodeStatus mainStatus) {
		this.mainStatus = mainStatus;
	}
}
