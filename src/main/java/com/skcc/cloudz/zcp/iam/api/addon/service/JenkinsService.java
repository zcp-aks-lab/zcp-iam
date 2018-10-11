package com.skcc.cloudz.zcp.iam.api.addon.service;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.skcc.cloudz.zcp.iam.api.addon.service.AddonService.NamespaceEventAdapter;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpErrorCode;
import com.skcc.cloudz.zcp.iam.common.exception.ZcpException;
import com.skcc.cloudz.zcp.iam.common.props.RoleProperties;
import com.skcc.cloudz.zcp.iam.manager.KeyCloakManager;

@Service
public class JenkinsService extends NamespaceEventAdapter {
	private final String JENKINS_API_CREATE_FOLDER = "{jenkins}/createItem?mode=com.cloudbees.hudson.plugins.folder.Folder&name={ns}";
	private final String JENKINS_API_GET_FOLDER = "{jenkins}/job/{ns}/config.xml";

	private final Logger log = LoggerFactory.getLogger(JenkinsService.class);
	private final RestTemplate rest = new RestTemplate();
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private KeyCloakManager keyCloakManager;

	@Autowired
	private RoleProperties roleMapping;
	
	@Value("${jenkins.url}")
	private String jenkinsUrl;

	@Value("${jenkins.token}")
	private String jenkinsToken;

	@Value("${jenkins.template.folder}")
	private String templatePath;
	
	@PostConstruct
	public void init() {
		String[] account = jenkinsToken.split(":");
		ClientHttpRequestInterceptor e = new BasicAuthorizationInterceptor(account[0], account[1]);
		rest.getInterceptors().add(e);
	}

	public void onCreateNamespace(String namespace) throws ZcpException {
		Map<String, String> roleMapping = getRoleMapping(namespace);
		
		// create namespace roles
		keyCloakManager.createRealmRoles(roleMapping.values());

		// create jenkins folder
		this.createJenkinsFolder(namespace, roleMapping);
	}

	public void onDeleteNamespace(String namespace) throws ZcpException {
		// create namespace roles
		Map<String, String> roles = getRoleMapping(namespace);
		keyCloakManager.deleteRealmRoles(roles.values());
	}
	
	public void verify(String namespace, Map<String, Object> ctx) throws ZcpException {
		Map<String, String> roleMapping = getRoleMapping(namespace);
		
		// create namespace roles
		if(!isDryRun(ctx)) {
			keyCloakManager.createRealmRoles(roleMapping.values());
		}
		log(ctx, "Create realm roles for namespace. {0}", roleMapping.values());

		// create jenkins folder
		// add context variables
		try {
			Map<String, String> vars = ImmutableMap.of("jenkins", jenkinsUrl, "ns", namespace);
			rest.getForEntity(JENKINS_API_GET_FOLDER, String.class, vars);

			log(ctx, "Skip to create Jenkins Folder for [{0}].", namespace);
		} catch(HttpClientErrorException e) {
			if(in(e, HttpStatus.NOT_FOUND)) {
				if(!isDryRun(ctx))
					this.createJenkinsFolder(namespace, roleMapping);

				log(ctx, "Create Jenkins Folder for [{0}].", namespace);
				return;
			}

			log.trace("", e);
			log(ctx, "Fail to create Jenkins Folder for [{0}, msg={1}].", namespace, e.getMessage());
		}
	}

	public void createJenkinsFolder(String namespace) throws ZcpException {
		createJenkinsFolder(namespace, getRoleMapping(namespace));
	}

	public void createJenkinsFolder(String namespace, Map<String, String> vars) throws ZcpException {
		try {
			// add context variables
			vars.put("jenkins", jenkinsUrl);
			vars.put("ns", namespace);

			// read config.xml template
			StringBuffer template = new StringBuffer();
			Resource resource = context.getResource(templatePath);
			Resources.asCharSource(resource.getURL(), Charset.forName("UTF-8")).copyTo(template);

			// create request
			URI uri = rest.getUriTemplateHandler().expand(JENKINS_API_CREATE_FOLDER, vars);
			String body = StringSubstitutor.replace(template, vars);
			RequestEntity<String> req = RequestEntity
					.post(uri)
					.contentType(MediaType.APPLICATION_XML)
					.body(body);
		
			// send request
			ResponseEntity<String> res = rest.exchange(req, String.class);
			log.debug("{}", res);
		} catch (IOException e) {
			log.error("", e);
			throw new ZcpException(ZcpErrorCode.INVALID_CONFIGURATION, e.getMessage());
		} catch(HttpClientErrorException e) {
			if(log.isDebugEnabled()) {
				log.debug(e.getMessage());
				log.debug("{}", e.getResponseHeaders());
				log.debug(e.getResponseBodyAsString());
			}
			
			if(in(e, HttpStatus.UNAUTHORIZED))
				throw new ZcpException(ZcpErrorCode.INVALID_CONFIGURATION, "Invalid password/token.");
			
			if(in(e, HttpStatus.FORBIDDEN))
				throw new ZcpException(ZcpErrorCode.NO_PERMISSON, "Missing the Job/Create permission.");

			if(in(e, HttpStatus.BAD_REQUEST))
				throw new ZcpException(ZcpErrorCode.INVALID_ARGUMENT, "A folder already exists with the name.");

			throw e;
		}
	}

	public Map<String, String> getRoleMapping(String namespace) {
		Map<String, String> vars = ImmutableMap.of("namespace", namespace);
		
		Map<String, String> mapping = Maps.newHashMap(roleMapping.getJenkins());
		mapping.entrySet().stream()
			.forEach(e -> {
				String v = StringSubstitutor.replace(e.getValue(), vars);
				e.setValue( v );
			});
		
		return mapping;
	}

	private boolean in(HttpClientErrorException e, HttpStatus status) {
		return e.getRawStatusCode() == status.value();
	}
}
