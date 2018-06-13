package com.skcc.cloudz.zcp.common.model;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

import io.kubernetes.client.models.V1ObjectMeta;
import io.swagger.annotations.ApiModel;

@ApiModel
public class V1alpha1NodeMetric {
	@SerializedName("apiVersion")
	private String apiVersion = null;

	@SerializedName("kind")
	private String kind = null;

	@SerializedName("metadata")
	private V1ObjectMeta metadata = null;

	@SerializedName("timestamp")
	private Date timestamp;

	@SerializedName("window")
	private String window;

	@SerializedName("usage")
	private V1alpha1Usage usage;
	
	public V1alpha1NodeMetric() {
		super();
		this.apiVersion = "v1alpha1";
		this.kind = "NodeMetric";
	}

	@ApiModel
	public class V1alpha1Usage {
		@SerializedName("cpu")
		private String cpu;
		@SerializedName("memory")
		private String memory;

		public String getCpu() {
			return cpu;
		}

		public void setCpu(String cpu) {
			this.cpu = cpu;
		}

		public String getMemory() {
			return memory;
		}

		public void setMemory(String memory) {
			this.memory = memory;
		}

	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public V1ObjectMeta getMetadata() {
		return metadata;
	}

	public void setMetadata(V1ObjectMeta metadata) {
		this.metadata = metadata;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getWindow() {
		return window;
	}

	public void setWindow(String window) {
		this.window = window;
	}

	public V1alpha1Usage getUsage() {
		return usage;
	}

	public void setUsage(V1alpha1Usage usage) {
		this.usage = usage;
	}

}
