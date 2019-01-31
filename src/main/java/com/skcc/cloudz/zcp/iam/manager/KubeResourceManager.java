package com.skcc.cloudz.zcp.iam.manager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.skcc.cloudz.zcp.iam.manager.client.ServiceAccountApiKeyAuth;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.AppsV1beta1Api;
import io.kubernetes.client.apis.AutoscalingV1Api;
import io.kubernetes.client.apis.BatchV1Api;
import io.kubernetes.client.apis.BatchV1beta1Api;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.apis.NetworkingV1Api;
import io.kubernetes.client.apis.RbacAuthorizationV1Api;
import io.kubernetes.client.apis.StorageV1Api;
import io.kubernetes.client.auth.Authentication;
import io.kubernetes.client.models.V1APIResourceList;
import io.kubernetes.client.util.ClientBuilder;

@Component
public class KubeResourceManager {
	private final Logger logger = (Logger) LoggerFactory.getLogger(KubeResourceManager.class);

	private ApiClient client;
	private Table<String, String, Object> mapping = HashBasedTable.create();

	@Value("${kube.client.api.output.pretty}")
	private String pretty;

	private ObjectMapper mapper = new ObjectMapper();

	public KubeResourceManager() throws Exception {
		ClientBuilder builder = ClientBuilder.standard();
		client = builder.build();

		// for jackson
		// https://stackoverflow.com/a/41645158
		mapper.registerModule(new JodaModule());

		// Create Api Object for each non CoreApi
		Object[] apis = {
		    new CoreV1Api(this.client),
			new AppsV1beta1Api(this.client),
			new AutoscalingV1Api(this.client),
			new BatchV1beta1Api(this.client),
			new BatchV1Api(this.client),
			new ExtensionsV1beta1Api(this.client),
			new NetworkingV1Api(this.client),
			new RbacAuthorizationV1Api(this.client),
			new StorageV1Api(this.client)
		};

		for(Object api : apis)
			mapping(mapping, api);
		
		/* START : RBAC Token Provider by User */
		Map<String, Authentication> auth = Maps.newHashMap(client.getAuthentications());
		auth.put("BearerToken", new ServiceAccountApiKeyAuth("header", "authorization"));
		auth = Collections.unmodifiableMap(auth);

		Field field = ReflectionUtils.findField(ApiClient.class, "authentications");
		ReflectionUtils.makeAccessible(field);
		ReflectionUtils.setField(field, client, auth);
		field.setAccessible(false);

		builder.getAuthentication().provide(client);
		/* END : RBAC Token Provider by User */

		logger.debug("KubeCoreManager is initialized");
	}

	public void mapping(Table<String, String, Object> mapping, Object api) throws Exception {
		V1APIResourceList resources = getAPIResources(api);
		System.out.println(new JSONObject(mapping.rowMap()));
		resources.getResources()
			.stream()
			.forEach(resource -> {
				mapping.put("kind", resource.getName(), resource.getKind());
				mapping.put("api", resource.getName(), api);
				mapping.put("namespaced", resource.getName(), resource.isNamespaced());

				if(resource.getShortNames() != null){
					for(String sh : resource.getShortNames()){
						mapping.put("kind", sh, resource.getKind());
						mapping.put("api", sh, api);
						mapping.put("namespaced", sh, resource.isNamespaced());
					}
				}

				String kind = resource.getKind().toLowerCase();
				mapping.put("kind", kind, resource.getKind());
				mapping.put("api", kind, api);
				mapping.put("namespaced", kind, resource.isNamespaced());
			});
	}

	private V1APIResourceList getAPIResources(Object api)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method method = BeanUtils.findDeclaredMethod(api.getClass(), "getAPIResources");
		return (V1APIResourceList) method.invoke(api);
	}

	public String toKind(String shortName) {
		// String cand = CaseUtils.toCamelCase(shortName, true);
		// try {
		// 	V1APIResourceList list = api.getAPIResources();
		// 	Optional<String> kind = list.getResources().stream()
		// 		.filter(r -> {
		// 			if (r.getName().equalsIgnoreCase(shortName))
		// 				return true;
		// 			if (r.getShortNames() != null && r.getShortNames().contains(shortName))
		// 				return true;
		//
		// 			return false;
		// 		})
		// 		.map(r -> r.getKind())
		// 		.findFirst();
		//
		// 	return kind.orElse(cand);
		// } catch (ApiException e) {
		// 	e.printStackTrace();
		// }
		//
		// return cand;
		return shortName;
	}

	public <T> T getList(String namespace, String alias) throws ApiException {
		Object api = mapping.get("api", alias);
		Boolean namespaced = (Boolean) mapping.get("namespaced", alias);
		String kind = (String) mapping.get("kind", alias);

		try {
			if(!namespaced){
				return (T) MethodUtils.invokeMethod(api, "list" + kind, pretty, null, null, null, null, null, null, null, null);
			} else {
				return (T) MethodUtils.invokeMethod(api, "listNamespaced" + kind, namespace, pretty, null, null, null, null, null, null, null, null);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		return null;
	}

	public <T> T getResource(String namespace, String alias, String name, String type) throws ApiException {
		Object api = mapping.get("api", alias);
		Boolean namespaced = (Boolean) mapping.get("namespaced", alias);
		String kind = (String) mapping.get("kind", alias);

		try {
			if(!namespaced){
				return (T) MethodUtils.invokeMethod(api, "read" + kind, name, pretty, false, false);
			} else {
				return (T) MethodUtils.invokeMethod(api, "readNamespaced" + kind , name, namespace, pretty, true, false);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		return null;
	}

	public <T> T updateResource(String namespace, String alias, String name, String json) throws Exception {
		Object api = mapping.get("api", alias);
		// Boolean namespaced = (Boolean) mapping.get("namespaced", alias);
		String kind = (String) mapping.get("kind", alias);

		try {
			String className = "io.kubernetes.client.models.V1" + kind;
			Class<?> clazz = ClassUtils.getClass(className);
			Object body = mapper.readValue(json, clazz);

			String methodName = "replaceNamespaced" + kind;
			return (T) MethodUtils.invokeMethod(api, methodName, name, namespace, body, pretty);
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
