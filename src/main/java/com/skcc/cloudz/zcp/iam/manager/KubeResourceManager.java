package com.skcc.cloudz.zcp.iam.manager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.collect.Maps;
import com.skcc.cloudz.zcp.iam.manager.client.ServiceAccountApiKeyAuth;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.text.CaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.auth.Authentication;
import io.kubernetes.client.models.V1APIResourceList;
import io.kubernetes.client.util.ClientBuilder;

@Component
public class KubeResourceManager {
	private final Logger logger = (Logger) LoggerFactory.getLogger(KubeResourceManager.class);

	private ApiClient client;
	private CoreV1Api api;

	@Value("${kube.client.api.output.pretty}")
	private String pretty;

	private ObjectMapper mapper = new ObjectMapper();

	public KubeResourceManager() throws IOException {
		ClientBuilder builder = ClientBuilder.standard();
		client = builder.build();
		
		Map<String, Authentication> auth = Maps.newHashMap(client.getAuthentications());
		auth.put("BearerToken", new ServiceAccountApiKeyAuth("header", "authorization"));
		auth = Collections.unmodifiableMap(auth);

		Field field = ReflectionUtils.findField(ApiClient.class, "authentications");
		ReflectionUtils.makeAccessible(field);
		ReflectionUtils.setField(field, client, auth);
		field.setAccessible(false);
		
		builder.getAuthentication().provide(client);

		api = new CoreV1Api(this.client);

		// for jackson
		// https://stackoverflow.com/a/41645158
		mapper.registerModule(new JodaModule());

		logger.debug("KubeCoreManager is initialized");
	}

	public String toKind(String shortName) {
		String cand = CaseUtils.toCamelCase(shortName, true);
		try {
			V1APIResourceList list = api.getAPIResources();
			Optional<String> kind = list.getResources().stream()
				.filter(r -> {
					if (r.getName().equalsIgnoreCase(shortName))
						return true;
					if (r.getShortNames() != null && r.getShortNames().contains(shortName))
						return true;
					
					return false;
				})
				.map(r -> r.getKind())
				.findFirst();

			return kind.orElse(cand);
		} catch (ApiException e) {
			e.printStackTrace();
		}

		return cand;
	}

	public <T> T getList(String namespace, String kind) throws ApiException {
		//api.listNamespacedConfigMap(namespace, pretty, _continue, fieldSelector, includeUninitialized, labelSelector, limit, resourceVersion, timeoutSeconds, watch)
		//api.listNamespacedSecret   (namespace, pretty, _continue, fieldSelector, includeUninitialized, labelSelector, limit, resourceVersion, timeoutSeconds, watch)

		try {
			if("namespace".equalsIgnoreCase(kind)){
				return (T) api.listNamespace(pretty, null, null, null, null, null, null, null, null);
			}
			return (T) MethodUtils.invokeMethod(api, "listNamespaced" + kind, namespace, pretty, null, null, null, null, null, null, null, null);
			//Method method = ReflectionUtils.findMethod(CoreV1Api.class, "listNamespaced" + kind);
			//return (T) method.invoke(api, namespace);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		return null;
	}

	public <T> T getResource(String namespace, String kind, String name, String type) throws ApiException {
		try {
			if("namespace".equalsIgnoreCase(kind)){
				return (T) api.readNamespace(name, pretty, false, false);
			}

			// api.readNamespacedPod(name, namespace, pretty, exact, export)
			// api.readNamespacedService(name, namespace, pretty, false, export)
			String methodName = "readNamespaced" + kind;
			return (T) MethodUtils.invokeMethod(api, methodName, name, namespace, pretty, true, false);
			//Method method = ReflectionUtils.findMethod(CoreV1Api.class, "listNamespaced" + kind);
			//return (T) method.invoke(api, namespace);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		return null;
	}

	public <T> T updateResource(String namespace, String kind, String name, String json) throws Exception {
		try {
			// if("namespace".equalsIgnoreCase(kind)){
			// 	return (T) api.readNamespace(name, pretty, false, false);
			// }


			String className = "io.kubernetes.client.models.V1" + kind;
			Class<?> clazz = ClassUtils.getClass(className);
			Object body = mapper.readValue(json, clazz);

			// api.replaceNamespacedPod(name, namespace, body, pretty)
			String methodName = "replaceNamespaced" + kind;
			return (T) MethodUtils.invokeMethod(api, methodName, name, namespace, body, pretty);
			//Method method = ReflectionUtils.findMethod(CoreV1Api.class, "listNamespaced" + kind);
			//return (T) method.invoke(api, namespace);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
			| NoSuchMethodException | ClassNotFoundException e) {
			logger.debug("", e);
			throw e;
		} catch (JsonParseException | JsonMappingException e) {
			logger.debug("", e);
			throw e;
		} catch (IOException e) {
			logger.debug("", e);
			throw e;
		}
	}
}
