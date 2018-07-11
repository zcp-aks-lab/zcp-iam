package com.skcc.cloudz.zcp.iam.api.metric.vo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.skcc.cloudz.zcp.iam.common.model.NodeStatus;
import com.skcc.cloudz.zcp.iam.common.model.NodeStatusMetric;
import com.skcc.cloudz.zcp.iam.common.util.NumberUtils;

public class NodesStatusMetricsVO {
	private String title = "Nodes";
	private List<NodeStatusMetric> statuses;
	private BigDecimal totalCount;
	private BigDecimal mainStatusPercentage;
	private NodeStatus mainStatus = NodeStatus.Ready;

	public List<NodeStatusMetric> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<NodeStatusMetric> statuses) {
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

			Map<NodeStatus, Integer> map = statuses.stream()
					.collect(Collectors.toMap(NodeStatusMetric::getStatus, NodeStatusMetric::getCount));
			double availableCount = map.get(mainStatus).doubleValue();

			return NumberUtils.percent(availableCount, totalCount.doubleValue());
		}
		return mainStatusPercentage;
	}

	public void setMainStatusPercentage(BigDecimal mainStatusPercentage) {
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
