package com.skcc.cloudz.zcp.iam.api.resource.controller;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.skcc.cloudz.zcp.iam.api.resource.service.ResourceService;
import com.skcc.cloudz.zcp.iam.manager.client.ServiceAccountApiKeyHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.kubernetes.client.custom.IntOrString;

@RestController
@RequestMapping("/iam")
public class ResourceController {
	private final Logger log = LoggerFactory.getLogger(ResourceController.class);

	@Autowired
	private ResourceService resourceService;
	
	@Value("${zcp.kube.namespace}")
	private String zcpSystemNamespace;

	@RequestMapping(value = "rbac/{username}/namespace", method = RequestMethod.GET)
	public Object namespaces(@PathVariable String username) throws Exception {
		ServiceAccountApiKeyHolder.instance().setToken(zcpSystemNamespace, username);
		return resourceService.getListNamespace(username);
	}

	@RequestMapping(value = "rbac/{username}/namespace/{namespace}/{kind}", method = RequestMethod.GET)
	public Object list(@PathVariable String namespace,
			@PathVariable String username,
			@PathVariable String kind) throws Exception {

		ServiceAccountApiKeyHolder.instance().setToken(zcpSystemNamespace, username);

		Object xxx = resourceService.getList(namespace, kind);
		log.debug("Response Type :: {}", xxx.getClass());

		return xxx;
	}

	@RequestMapping(value = "rbac/{username}/namespace/{namespace}/{kind}/{name}", method = RequestMethod.GET)
	public Object getResource(@PathVariable String namespace,
			@PathVariable String username,
			@PathVariable String kind,
			@PathVariable String name,
			@RequestParam String type) throws Exception {

		ServiceAccountApiKeyHolder.instance().setToken(zcpSystemNamespace, username);

		Object xxx = resourceService.getResource(namespace, kind, name, type);
		log.debug("Response Type :: {}", xxx.getClass());

		return xxx;
	}

	@RequestMapping(value = "rbac/{username}/namespace/{namespace}/{kind}/{name}", method = RequestMethod.PUT)
	public Object putResource(@PathVariable String namespace,
			@PathVariable String username,
			@PathVariable String kind,
			@PathVariable String name,
			@RequestBody String json) throws Exception {

		ServiceAccountApiKeyHolder.instance().setToken(zcpSystemNamespace, username);

		Object xxx = resourceService.updateResource(namespace, kind, name, json);
		log.debug("Response Type :: {}", xxx.getClass());

		return xxx;
	}

	@GetMapping(value = "rbac/{username}/namespace/{namespace}/{kind}/{name}/logs")
	public Object getLogs(@PathVariable String namespace,
			@PathVariable String username,
			@PathVariable String kind,
			@PathVariable String name,
			@RequestParam Map<String, Object> param) throws Exception {

		ServiceAccountApiKeyHolder.instance().setToken(zcpSystemNamespace, username);

		param.put("namespace", namespace);
		param.put("name", name);
		// InputStream is = resourceService.getLogs(param);
		// InputStreamResource resource = new InputStreamResource(is);
		String ret = resourceService.getLogs(param);

		return ret;
	}

	@JsonComponent
	public class IntOrStringSerializer extends JsonSerializer<IntOrString> {
		/*
		 * https://github.com/kubernetes-client/java/issues/177
		 * https://github.com/kubernetes-client/java/blob/master/kubernetes/src/main/java/io/kubernetes/client/custom/IntOrString.java#L52-L73
		 * 
		 * https://homoefficio.github.io/2016/11/18/%EC%95%8C%EA%B3%A0%EB%B3%B4%EB%A9%B4-%EB%A7%8C%EB%A7%8C%ED%95%9C-Jackson-Custom-Serialization/
		 */
		public void serialize(IntOrString value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
			if(value.isInteger()){
				gen.writeNumber(value.getIntValue());
			} else {
				gen.writeString(value.getStrValue());
			}
		}
	};
}
