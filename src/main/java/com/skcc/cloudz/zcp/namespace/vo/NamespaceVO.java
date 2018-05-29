package com.skcc.cloudz.zcp.namespace.vo;

import com.skcc.cloudz.zcp.common.vo.Ivo;

import io.kubernetes.client.models.V1LimitRange;
import io.kubernetes.client.models.V1ResourceQuota;

public class NamespaceVO  implements Ivo{
	String namespace;
	ResourceQuotaVO resourceQuota;
	LimitRangeVO limitRange;
	
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public ResourceQuotaVO getResourceQuota() {
		return resourceQuota;
	}

	public void setResourceQuota(ResourceQuotaVO resourceQuota) {
		this.resourceQuota = resourceQuota;
	}

	public LimitRangeVO getLimitRange() {
		return limitRange;
	}

	public void setLimitRange(LimitRangeVO limitRange) {
		this.limitRange = limitRange;
	}
	
	public class ResourceQuotaVO extends V1ResourceQuota{
		String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
	}
	
	public class LimitRangeVO extends V1LimitRange{
		String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
	}
}
