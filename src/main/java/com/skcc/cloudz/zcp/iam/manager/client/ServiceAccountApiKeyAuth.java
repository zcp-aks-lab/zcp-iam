package com.skcc.cloudz.zcp.iam.manager.client;

import java.util.List;
import java.util.Map;

import io.kubernetes.client.Pair;
import io.kubernetes.client.auth.ApiKeyAuth;

public class ServiceAccountApiKeyAuth extends ApiKeyAuth {
	public ServiceAccountApiKeyAuth(String location, String paramName) {
		super(location, paramName);
	}
	
	@Override
	public String getApiKey() {
		return ServiceAccountApiKeyHolder.instance().getToken();
	}

	@Override
	public void applyToParams(List<Pair> queryParams, Map<String, String> headerParams) {
		String location = getLocation();
		String paramName = getParamName();
		String apiKeyPrefix = getApiKeyPrefix();
		String apiKey = getApiKey();

		// copy from super.applyToParams(...)
		if (apiKey == null) {
			return;
		}
		String value;
		if (apiKeyPrefix != null) {
			value = apiKeyPrefix + " " + apiKey;
		} else {
			value = apiKey;
		}
		if ("query".equals(location)) {
			queryParams.add(new Pair(paramName, value));
		} else if ("header".equals(location)) {
			headerParams.put(paramName, value);
		}
	}
}
