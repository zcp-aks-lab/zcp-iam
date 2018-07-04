package com.skcc.cloudz.zcp.metric.vo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.skcc.cloudz.zcp.common.model.NodeStatus;
import com.skcc.cloudz.zcp.common.model.NodeStatusMetric;

public class NodesStatusMetricsVO {
	private List<NodeStatusMetric> statuses;
	private Integer totalCount;
	private Integer availablePercentage = -1;

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

	public Integer getAvailablePercentage() {
		if (availablePercentage < 0) {
			if (statuses == null)
				return availablePercentage;
			if (totalCount == 0)
				return availablePercentage;

			Map<NodeStatus, Integer> map = statuses.stream()
					.collect(Collectors.toMap(NodeStatusMetric::getStatus, NodeStatusMetric::getCount));
			int availableCount = map.get(NodeStatus.Ready).intValue();
			
			return (int) (availableCount / totalCount) * 100;
		}
		return availablePercentage;
	}

	public void setAvailablePercentage(Integer availablePercentage) {
		this.availablePercentage = availablePercentage;
	}
}
