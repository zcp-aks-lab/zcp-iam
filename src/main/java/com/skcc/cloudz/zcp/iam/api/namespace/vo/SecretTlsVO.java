package com.skcc.cloudz.zcp.iam.api.namespace.vo;

import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

public class SecretTlsVO implements SecretVO {
	@NotNull
	private String name;

	private String type = "kubernetes.io/tls";

	private String description;

	@NotNull
	private MultipartFile crt;
	@NotNull
	private MultipartFile key;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public MultipartFile getCrt() {
		return crt;
	}
	public void setCrt(MultipartFile crt) {
		this.crt = crt;
	}
	public MultipartFile getKey() {
		return key;
	}
	public void setKey(MultipartFile key) {
		this.key = key;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
