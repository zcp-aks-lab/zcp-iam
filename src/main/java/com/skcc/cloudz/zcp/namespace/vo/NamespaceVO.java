package com.skcc.cloudz.zcp.namespace.vo;

import com.skcc.cloudz.zcp.common.vo.Ivo;

import io.kubernetes.client.models.V1LimitRange;
import io.kubernetes.client.models.V1ResourceQuota;

public class NamespaceVO  implements Ivo{
	String namespace;
	V1ResourceQuota resourceQuota;
	V1LimitRange limitRange;
	
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public V1ResourceQuota getResourceQuota() {
		return resourceQuota;
	}

	public void setResourceQuota(V1ResourceQuota resourceQuota) {
		this.resourceQuota = resourceQuota;
	}

	public V1LimitRange getLimitRange() {
		return limitRange;
	}

	public void setLimitRange(V1LimitRange limitRange) {
		this.limitRange = limitRange;
	}
	
//	public class ResourceQuotaVO extends V1ResourceQuota{
//		String name;
//
//		public String getName() {
//			return name;
//		}
//
//		public void setName(String name) {
//			this.name = name;
//		}
//		
//	}
//	
//	public class LimitRangeVO extends V1LimitRange{
//		String name;
//
//		public String getName() {
//			return name;
//		}
//
//		public void setName(String name) {
//			this.name = name;
//		}
//		
//	}
}
