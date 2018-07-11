package com.skcc.cloudz.zcp.iam.common.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ZcpKubeConfig {
	private String apiVersion;
	private String kind;

	private List<ClusterInfo> clusters;
	private List<ContextInfo> contexts;
	private List<UserInfo> users;
	private List<PreferenceInfo> preferences;

	@JsonProperty("current-context")
	private String currentContext;

	public ZcpKubeConfig() {
		clusters = new ArrayList<>();
		contexts = new ArrayList<>();
		users = new ArrayList<>();
//		preferences = new ArrayList<>();
	}

	public class ContextInfo {
		private String name;
		private Context context;

		public class Context {
			private String cluster;
			private String user;
			private String namespace;

			public String getNamespace() {
				return namespace;
			}

			public void setNamespace(String namespace) {
				this.namespace = namespace;
			}

			public String getCluster() {
				return cluster;
			}

			public void setCluster(String cluster) {
				this.cluster = cluster;
			}

			public String getUser() {
				return user;
			}

			public void setUser(String user) {
				this.user = user;
			}
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Context getContext() {
			return context;
		}

		public void setContext(Context context) {
			this.context = context;
		}

	}

	public class UserInfo {
		private String name;
		private User user;

		public class User {
			private String token;

			public String getToken() {
				return token;
			}

			public void setToken(String token) {
				this.token = token;
			}

		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public User getUser() {
			return user;
		}

		public void setUser(User user) {
			this.user = user;
		}

	}

	public class ClusterInfo {
		private String name;
		private Cluster cluster;

		public class Cluster {
			@JsonProperty("certificate-authority-data")
			private String certificateAuthorityData;
			private String server;

			public String getCertificateAuthorityData() {
				return certificateAuthorityData;
			}

			public void setCertificateAuthorityData(String certificateAuthorityData) {
				this.certificateAuthorityData = certificateAuthorityData;
			}

			public String getServer() {
				return server;
			}

			public void setServer(String server) {
				this.server = server;
			}

		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Cluster getCluster() {
			return cluster;
		}

		public void setCluster(Cluster cluster) {
			this.cluster = cluster;
		}

	}
	
	public class PreferenceInfo {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
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

	public List<ClusterInfo> getClusters() {
		return clusters;
	}

	public void setClusters(List<ClusterInfo> clusters) {
		this.clusters = clusters;
	}

	public List<ContextInfo> getContexts() {
		return contexts;
	}

	public void setContexts(List<ContextInfo> contexts) {
		this.contexts = contexts;
	}

	public List<UserInfo> getUsers() {
		return users;
	}

	public void setUsers(List<UserInfo> users) {
		this.users = users;
	}

	public String getCurrentContext() {
		return currentContext;
	}

	public void setCurrentContext(String currentContext) {
		this.currentContext = currentContext;
	}

	public List<PreferenceInfo> getPreferences() {
		return preferences;
	}

	public void setPreferences(List<PreferenceInfo> preferences) {
		this.preferences = preferences;
	}

}
