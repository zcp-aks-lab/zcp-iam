package com.skcc.cloudz.zcp.iam.api.metric.vo;

import java.math.BigDecimal;

public class ClusterStatusMetricsVO {

	private String title;
	private BigDecimal utilization;
	private BigDecimal available;
	private BigDecimal total;
	private BigDecimal utilizationPercentage;
	private String utilizationTitle = "Utilization";
	private String unit;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public BigDecimal getUtilization() {
		return utilization;
	}

	public void setUtilization(BigDecimal utilization) {
		this.utilization = utilization;
	}

	public BigDecimal getAvailable() {
		return available;
	}

	public void setAvailable(BigDecimal available) {
		this.available = available;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public BigDecimal getUtilizationPercentage() {
		return utilizationPercentage;
	}

	public void setUtilizationPercentage(BigDecimal utilizationPercentage) {
		this.utilizationPercentage = utilizationPercentage;
	}

	public String getUtilizationTitle() {
		return utilizationTitle;
	}

	public void setUtilizationTitle(String utilizationTitle) {
		this.utilizationTitle = utilizationTitle;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

}
