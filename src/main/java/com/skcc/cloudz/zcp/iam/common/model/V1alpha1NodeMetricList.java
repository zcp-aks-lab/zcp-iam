package com.skcc.cloudz.zcp.iam.common.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import io.kubernetes.client.models.V1ListMeta;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "NodeMetricList is a list of NodeMetric objects")
public class V1alpha1NodeMetricList {

	@SerializedName("apiVersion")
	private String apiVersion = null;

	@SerializedName("items")
	private List<V1alpha1NodeMetric> items = new ArrayList<V1alpha1NodeMetric>();

	@SerializedName("kind")
	private String kind = null;

	@SerializedName("metadata")
	private V1ListMeta metadata = null;

	public V1alpha1NodeMetricList() {
		super();
		this.apiVersion = "v1alpha1";
		this.kind = "Metics";
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public List<V1alpha1NodeMetric> getItems() {
		return items;
	}

	public void setItems(List<V1alpha1NodeMetric> items) {
		this.items = items;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public V1ListMeta getMetadata() {
		return metadata;
	}

	public void setMetadata(V1ListMeta metadata) {
		this.metadata = metadata;
	}

}
