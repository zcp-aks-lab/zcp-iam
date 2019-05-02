package com.skcc.cloudz.zcp.iam.manager.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.skcc.cloudz.zcp.iam.common.model.V1alpha1NodeMetricList;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.ApiResponse;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.Pair;
import io.kubernetes.client.ProgressRequestBody;
import io.kubernetes.client.ProgressResponseBody;

public class MetricV1alph1Api {
	private ApiClient apiClient;

	public MetricV1alph1Api() {
		this(Configuration.getDefaultApiClient());
	}

	public MetricV1alph1Api(ApiClient apiClient) {
		this.apiClient = apiClient;
	}

	public ApiClient getApiClient() {
		return apiClient;
	}

	public void setApiClient(ApiClient apiClient) {
		this.apiClient = apiClient;
	}

	
	/**
	 * Build call for listNamespacedService
	 * 
	 * @param namespace
	 *            object name and auth scope, such as for teams and projects
	 *            (required)
	 * @param pretty
	 *            If &#39;true&#39;, then the output is pretty printed. (optional)
	 * @param _continue
	 *            The continue option should be set when retrieving more results
	 *            from the server. Since this value is server defined, clients may
	 *            only use the continue value from a previous query result with
	 *            identical query parameters (except for the value of continue) and
	 *            the server may reject a continue value it does not recognize. If
	 *            the specified continue value is no longer valid whether due to
	 *            expiration (generally five to fifteen minutes) or a configuration
	 *            change on the server the server will respond with a 410
	 *            ResourceExpired error indicating the client must restart their
	 *            list without the continue field. This field is not supported when
	 *            watch is true. Clients may start a watch from the last
	 *            resourceVersion value returned by the server and not miss any
	 *            modifications. (optional)
	 * @param fieldSelector
	 *            A selector to restrict the list of returned objects by their
	 *            fields. Defaults to everything. (optional)
	 * @param includeUninitialized
	 *            If true, partially initialized resources are included in the
	 *            response. (optional)
	 * @param labelSelector
	 *            A selector to restrict the list of returned objects by their
	 *            labels. Defaults to everything. (optional)
	 * @param limit
	 *            limit is a maximum number of responses to return for a list call.
	 *            If more items exist, the server will set the &#x60;continue&#x60;
	 *            field on the list metadata to a value that can be used with the
	 *            same initial query to retrieve the next set of results. Setting a
	 *            limit may return fewer than the requested amount of items (up to
	 *            zero items) in the event all requested objects are filtered out
	 *            and clients should only use the presence of the continue field to
	 *            determine whether more results are available. Servers may choose
	 *            not to support the limit argument and will return all of the
	 *            available results. If limit is specified and the continue field is
	 *            empty, clients may assume that no more results are available. This
	 *            field is not supported if watch is true. The server guarantees
	 *            that the objects returned when using continue will be identical to
	 *            issuing a single list call without a limit - that is, no objects
	 *            created, modified, or deleted after the first request is issued
	 *            will be included in any subsequent continued requests. This is
	 *            sometimes referred to as a consistent snapshot, and ensures that a
	 *            client that is using limit to receive smaller chunks of a very
	 *            large result can ensure they see all possible objects. If objects
	 *            are updated during a chunked list the version of the object that
	 *            was present at the time the first list result was calculated is
	 *            returned. (optional)
	 * @param resourceVersion
	 *            When specified with a watch call, shows changes that occur after
	 *            that particular version of a resource. Defaults to changes from
	 *            the beginning of history. When specified for list: - if unset,
	 *            then the result is returned from remote storage based on
	 *            quorum-read flag; - if it&#39;s 0, then we simply return what we
	 *            currently have in cache, no guarantee; - if set to non zero, then
	 *            the result is at least as fresh as given rv. (optional)
	 * @param timeoutSeconds
	 *            Timeout for the list/watch call. (optional)
	 * @param watch
	 *            Watch for changes to the described resources and return them as a
	 *            stream of add, update, and remove notifications. Specify
	 *            resourceVersion. (optional)
	 * @param kubeMetric
	 *            Type of kubernetes metrics.
	 *            k8s version ~1.11 is heaster, version 1.12~ metrics-server.
	 *            default is metrics-servier
	 * @param progressListener
	 *            Progress listener
	 * @param progressRequestListener
	 *            Progress request listener
	 * @return Call to execute
	 * @throws ApiException
	 *             If fail to serialize the request body object
	 */
	public com.squareup.okhttp.Call listNodeMetricsCall(String namespace, String pretty, String _continue,
			String fieldSelector, Boolean includeUninitialized, String labelSelector, Integer limit,
			String resourceVersion, Integer timeoutSeconds, Boolean watch,
			final ProgressResponseBody.ProgressListener progressListener,
			final ProgressRequestBody.ProgressRequestListener progressRequestListener, String kubeMetric) throws ApiException {
		Object localVarPostBody = null;

		// create path and map variables
		
		// (default) metircs by 'metrics-server' > kube version 1.12~ 
		String localVarPath = "/apis/metrics.k8s.io/v1beta1/nodes";
		if (kubeMetric != null) {
			// metrics by 'heapster' > kube version ~1.11
			if ("heapster".equals(kubeMetric.toLowerCase())) {
				localVarPath = "/api/v1/namespaces/kube-system/services/http:heapster:/proxy/apis/metrics/v1alpha1/nodes";
			}
		}

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		if (pretty != null)
			localVarQueryParams.addAll(apiClient.parameterToPair("pretty", pretty));
		if (_continue != null)
			localVarQueryParams.addAll(apiClient.parameterToPair("continue", _continue));
		if (fieldSelector != null)
			localVarQueryParams.addAll(apiClient.parameterToPair("fieldSelector", fieldSelector));
		if (includeUninitialized != null)
			localVarQueryParams.addAll(apiClient.parameterToPair("includeUninitialized", includeUninitialized));
		if (labelSelector != null)
			localVarQueryParams.addAll(apiClient.parameterToPair("labelSelector", labelSelector));
		if (limit != null)
			localVarQueryParams.addAll(apiClient.parameterToPair("limit", limit));
		if (resourceVersion != null)
			localVarQueryParams.addAll(apiClient.parameterToPair("resourceVersion", resourceVersion));
		if (timeoutSeconds != null)
			localVarQueryParams.addAll(apiClient.parameterToPair("timeoutSeconds", timeoutSeconds));
		if (watch != null)
			localVarQueryParams.addAll(apiClient.parameterToPair("watch", watch));

		Map<String, String> localVarHeaderParams = new HashMap<String, String>();

		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = { "application/json", "application/yaml",
				"application/vnd.kubernetes.protobuf", "application/json;stream=watch",
				"application/vnd.kubernetes.protobuf;stream=watch" };
		final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null)
			localVarHeaderParams.put("Accept", localVarAccept);

		final String[] localVarContentTypes = { "*/*" };
		final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
		localVarHeaderParams.put("Content-Type", localVarContentType);

		if (progressListener != null) {
			apiClient.getHttpClient().networkInterceptors().add(new com.squareup.okhttp.Interceptor() {
				@Override
				public com.squareup.okhttp.Response intercept(com.squareup.okhttp.Interceptor.Chain chain)
						throws IOException {
					com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
					return originalResponse.newBuilder().body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
				}
			});
		}

		String[] localVarAuthNames = new String[] { "BearerToken" };
		return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
	}
	
	@Deprecated
	public com.squareup.okhttp.Call listNodeMetricsCall(String namespace, String pretty, String _continue,
			String fieldSelector, Boolean includeUninitialized, String labelSelector, Integer limit,
			String resourceVersion, Integer timeoutSeconds, Boolean watch,
			final ProgressResponseBody.ProgressListener progressListener,
			final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
		Object localVarPostBody = null;

		// create path and map variables
//		String localVarPath = "/api/v1/namespaces/kube-system/services/http:heapster:/proxy/apis/metrics/v1alpha1/nodes";
		String localVarPath = "/apis/metrics.k8s.io/v1beta1/nodes";
		// .replaceAll("\\{" + "namespace" + "\\}",
		// apiClient.escapeString(namespace.toString()));

		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
		if (pretty != null)
			localVarQueryParams.addAll(apiClient.parameterToPair("pretty", pretty));
		if (_continue != null)
			localVarQueryParams.addAll(apiClient.parameterToPair("continue", _continue));
		if (fieldSelector != null)
			localVarQueryParams.addAll(apiClient.parameterToPair("fieldSelector", fieldSelector));
		if (includeUninitialized != null)
			localVarQueryParams.addAll(apiClient.parameterToPair("includeUninitialized", includeUninitialized));
		if (labelSelector != null)
			localVarQueryParams.addAll(apiClient.parameterToPair("labelSelector", labelSelector));
		if (limit != null)
			localVarQueryParams.addAll(apiClient.parameterToPair("limit", limit));
		if (resourceVersion != null)
			localVarQueryParams.addAll(apiClient.parameterToPair("resourceVersion", resourceVersion));
		if (timeoutSeconds != null)
			localVarQueryParams.addAll(apiClient.parameterToPair("timeoutSeconds", timeoutSeconds));
		if (watch != null)
			localVarQueryParams.addAll(apiClient.parameterToPair("watch", watch));

		Map<String, String> localVarHeaderParams = new HashMap<String, String>();

		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String[] localVarAccepts = { "application/json", "application/yaml",
				"application/vnd.kubernetes.protobuf", "application/json;stream=watch",
				"application/vnd.kubernetes.protobuf;stream=watch" };
		final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		if (localVarAccept != null)
			localVarHeaderParams.put("Accept", localVarAccept);

		final String[] localVarContentTypes = { "*/*" };
		final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
		localVarHeaderParams.put("Content-Type", localVarContentType);

		if (progressListener != null) {
			apiClient.getHttpClient().networkInterceptors().add(new com.squareup.okhttp.Interceptor() {
				@Override
				public com.squareup.okhttp.Response intercept(com.squareup.okhttp.Interceptor.Chain chain)
						throws IOException {
					com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
					return originalResponse.newBuilder()
							.body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
				}
			});
		}

		String[] localVarAuthNames = new String[] { "BearerToken" };
		return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams,
				localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
	}
	
	
	/**
	 * 
	 * list or watch objects of kind Service
	 * 
	 * @param namespace
	 *            object name and auth scope, such as for teams and projects
	 *            (required)
	 * @param pretty
	 *            If &#39;true&#39;, then the output is pretty printed. (optional)
	 * @param _continue
	 *            The continue option should be set when retrieving more results
	 *            from the server. Since this value is server defined, clients may
	 *            only use the continue value from a previous query result with
	 *            identical query parameters (except for the value of continue) and
	 *            the server may reject a continue value it does not recognize. If
	 *            the specified continue value is no longer valid whether due to
	 *            expiration (generally five to fifteen minutes) or a configuration
	 *            change on the server the server will respond with a 410
	 *            ResourceExpired error indicating the client must restart their
	 *            list without the continue field. This field is not supported when
	 *            watch is true. Clients may start a watch from the last
	 *            resourceVersion value returned by the server and not miss any
	 *            modifications. (optional)
	 * @param fieldSelector
	 *            A selector to restrict the list of returned objects by their
	 *            fields. Defaults to everything. (optional)
	 * @param includeUninitialized
	 *            If true, partially initialized resources are included in the
	 *            response. (optional)
	 * @param labelSelector
	 *            A selector to restrict the list of returned objects by their
	 *            labels. Defaults to everything. (optional)
	 * @param limit
	 *            limit is a maximum number of responses to return for a list call.
	 *            If more items exist, the server will set the &#x60;continue&#x60;
	 *            field on the list metadata to a value that can be used with the
	 *            same initial query to retrieve the next set of results. Setting a
	 *            limit may return fewer than the requested amount of items (up to
	 *            zero items) in the event all requested objects are filtered out
	 *            and clients should only use the presence of the continue field to
	 *            determine whether more results are available. Servers may choose
	 *            not to support the limit argument and will return all of the
	 *            available results. If limit is specified and the continue field is
	 *            empty, clients may assume that no more results are available. This
	 *            field is not supported if watch is true. The server guarantees
	 *            that the objects returned when using continue will be identical to
	 *            issuing a single list call without a limit - that is, no objects
	 *            created, modified, or deleted after the first request is issued
	 *            will be included in any subsequent continued requests. This is
	 *            sometimes referred to as a consistent snapshot, and ensures that a
	 *            client that is using limit to receive smaller chunks of a very
	 *            large result can ensure they see all possible objects. If objects
	 *            are updated during a chunked list the version of the object that
	 *            was present at the time the first list result was calculated is
	 *            returned. (optional)
	 * @param resourceVersion
	 *            When specified with a watch call, shows changes that occur after
	 *            that particular version of a resource. Defaults to changes from
	 *            the beginning of history. When specified for list: - if unset,
	 *            then the result is returned from remote storage based on
	 *            quorum-read flag; - if it&#39;s 0, then we simply return what we
	 *            currently have in cache, no guarantee; - if set to non zero, then
	 *            the result is at least as fresh as given rv. (optional)
	 * @param timeoutSeconds
	 *            Timeout for the list/watch call. (optional)
	 * @param watch
	 *            Watch for changes to the described resources and return them as a
	 *            stream of add, update, and remove notifications. Specify
	 *            resourceVersion. (optional)
	 * @param kubeMetric
	 *            Type of kubernetes metrics.
	 *            k8s version ~1.11 is heaster, version 1.12~ metrics-server.
	 *            default is metrics-servier
	 * @return V1ServiceList
	 * @throws ApiException
	 *             If fail to call the API, e.g. server error or cannot deserialize
	 *             the response body
	 */
	public V1alpha1NodeMetricList listNodeMetrics(String namespace, String pretty, String _continue, String fieldSelector,
			Boolean includeUninitialized, String labelSelector, Integer limit, String resourceVersion,
			Integer timeoutSeconds, Boolean watch, String kubeMetric) throws ApiException {
		ApiResponse<V1alpha1NodeMetricList> resp = listNodeMetricsWithHttpInfo(namespace, pretty, _continue, fieldSelector, includeUninitialized, labelSelector, limit, resourceVersion, timeoutSeconds, watch, kubeMetric);
		return resp.getData();
	}
	
	@Deprecated
	public V1alpha1NodeMetricList listNodeMetrics(String namespace, String pretty, String _continue, String fieldSelector,
			Boolean includeUninitialized, String labelSelector, Integer limit, String resourceVersion,
			Integer timeoutSeconds, Boolean watch) throws ApiException {
		ApiResponse<V1alpha1NodeMetricList> resp = listNodeMetricsWithHttpInfo(namespace, pretty, _continue, fieldSelector,
				includeUninitialized, labelSelector, limit, resourceVersion, timeoutSeconds, watch);
		return resp.getData();
	}

	
	/**
	 * 
	 * list or watch objects of kind Service
	 * 
	 * @param namespace
	 *            object name and auth scope, such as for teams and projects
	 *            (required)
	 * @param pretty
	 *            If &#39;true&#39;, then the output is pretty printed. (optional)
	 * @param _continue
	 *            The continue option should be set when retrieving more results
	 *            from the server. Since this value is server defined, clients may
	 *            only use the continue value from a previous query result with
	 *            identical query parameters (except for the value of continue) and
	 *            the server may reject a continue value it does not recognize. If
	 *            the specified continue value is no longer valid whether due to
	 *            expiration (generally five to fifteen minutes) or a configuration
	 *            change on the server the server will respond with a 410
	 *            ResourceExpired error indicating the client must restart their
	 *            list without the continue field. This field is not supported when
	 *            watch is true. Clients may start a watch from the last
	 *            resourceVersion value returned by the server and not miss any
	 *            modifications. (optional)
	 * @param fieldSelector
	 *            A selector to restrict the list of returned objects by their
	 *            fields. Defaults to everything. (optional)
	 * @param includeUninitialized
	 *            If true, partially initialized resources are included in the
	 *            response. (optional)
	 * @param labelSelector
	 *            A selector to restrict the list of returned objects by their
	 *            labels. Defaults to everything. (optional)
	 * @param limit
	 *            limit is a maximum number of responses to return for a list call.
	 *            If more items exist, the server will set the &#x60;continue&#x60;
	 *            field on the list metadata to a value that can be used with the
	 *            same initial query to retrieve the next set of results. Setting a
	 *            limit may return fewer than the requested amount of items (up to
	 *            zero items) in the event all requested objects are filtered out
	 *            and clients should only use the presence of the continue field to
	 *            determine whether more results are available. Servers may choose
	 *            not to support the limit argument and will return all of the
	 *            available results. If limit is specified and the continue field is
	 *            empty, clients may assume that no more results are available. This
	 *            field is not supported if watch is true. The server guarantees
	 *            that the objects returned when using continue will be identical to
	 *            issuing a single list call without a limit - that is, no objects
	 *            created, modified, or deleted after the first request is issued
	 *            will be included in any subsequent continued requests. This is
	 *            sometimes referred to as a consistent snapshot, and ensures that a
	 *            client that is using limit to receive smaller chunks of a very
	 *            large result can ensure they see all possible objects. If objects
	 *            are updated during a chunked list the version of the object that
	 *            was present at the time the first list result was calculated is
	 *            returned. (optional)
	 * @param resourceVersion
	 *            When specified with a watch call, shows changes that occur after
	 *            that particular version of a resource. Defaults to changes from
	 *            the beginning of history. When specified for list: - if unset,
	 *            then the result is returned from remote storage based on
	 *            quorum-read flag; - if it&#39;s 0, then we simply return what we
	 *            currently have in cache, no guarantee; - if set to non zero, then
	 *            the result is at least as fresh as given rv. (optional)
	 * @param timeoutSeconds
	 *            Timeout for the list/watch call. (optional)
	 * @param watch
	 *            Watch for changes to the described resources and return them as a
	 *            stream of add, update, and remove notifications. Specify
	 *            resourceVersion. (optional)
	 * @param kubeMetric
	 *            Type of kubernetes metrics.
	 *            k8s version ~1.11 is heaster, version 1.12~ metrics-server.
	 *            default is metrics-servier.
	 * @return ApiResponse&lt;V1ServiceList&gt;
	 * @throws ApiException
	 *             If fail to call the API, e.g. server error or cannot deserialize
	 *             the response body
	 */
	public ApiResponse<V1alpha1NodeMetricList> listNodeMetricsWithHttpInfo(String namespace, String pretty,
			String _continue, String fieldSelector, Boolean includeUninitialized, String labelSelector, Integer limit,
			String resourceVersion, Integer timeoutSeconds, Boolean watch, String kubeMetric) throws ApiException {
		com.squareup.okhttp.Call call = listNodeMetricsValidateBeforeCall(namespace, pretty, _continue, fieldSelector, includeUninitialized, labelSelector, limit, resourceVersion, timeoutSeconds, watch, null, null, kubeMetric);
		Type localVarReturnType = new TypeToken<V1alpha1NodeMetricList>() { }.getType();
		return apiClient.execute(call, localVarReturnType);
	}
	
	@Deprecated
	public ApiResponse<V1alpha1NodeMetricList> listNodeMetricsWithHttpInfo(String namespace, String pretty,
			String _continue, String fieldSelector, Boolean includeUninitialized, String labelSelector, Integer limit,
			String resourceVersion, Integer timeoutSeconds, Boolean watch) throws ApiException {
		com.squareup.okhttp.Call call = listNodeMetricsValidateBeforeCall(namespace, pretty, _continue,
				fieldSelector, includeUninitialized, labelSelector, limit, resourceVersion, timeoutSeconds, watch, null,
				null);
		Type localVarReturnType = new TypeToken<V1alpha1NodeMetricList>() { }.getType();
		return apiClient.execute(call, localVarReturnType);
	}
	
	
	private com.squareup.okhttp.Call listNodeMetricsValidateBeforeCall(String namespace, String pretty,
			String _continue, String fieldSelector, Boolean includeUninitialized, String labelSelector, Integer limit,
			String resourceVersion, Integer timeoutSeconds, Boolean watch,
			final ProgressResponseBody.ProgressListener progressListener,
			final ProgressRequestBody.ProgressRequestListener progressRequestListener, String kubeMetric) throws ApiException {

		com.squareup.okhttp.Call call = listNodeMetricsCall(namespace, pretty, _continue, fieldSelector, includeUninitialized, labelSelector, limit, resourceVersion, timeoutSeconds, watch, progressListener, progressRequestListener, kubeMetric);
		return call;

	}

	@Deprecated
	private com.squareup.okhttp.Call listNodeMetricsValidateBeforeCall(String namespace, String pretty,
			String _continue, String fieldSelector, Boolean includeUninitialized, String labelSelector, Integer limit,
			String resourceVersion, Integer timeoutSeconds, Boolean watch,
			final ProgressResponseBody.ProgressListener progressListener,
			final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {

		// verify the required parameter 'namespace' is set
//		if (namespace == null) {
//			throw new ApiException(
//					"Missing the required parameter 'namespace' when calling listNamespacedService(Async)");
//		}

		com.squareup.okhttp.Call call = listNodeMetricsCall(namespace, pretty, _continue, fieldSelector,
				includeUninitialized, labelSelector, limit, resourceVersion, timeoutSeconds, watch, progressListener,
				progressRequestListener);
		return call;

	}
	

}
