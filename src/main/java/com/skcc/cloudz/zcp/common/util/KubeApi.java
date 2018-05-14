package com.skcc.cloudz.zcp.common.util;

import io.kubernetes.client.ApiException;

public interface KubeApi {
	Object getData() throws ApiException;
		
}
