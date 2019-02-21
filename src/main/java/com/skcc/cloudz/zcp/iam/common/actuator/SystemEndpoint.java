package com.skcc.cloudz.zcp.iam.common.actuator;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.mvc.AbstractMvcEndpoint;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;

@Component
public class SystemEndpoint extends AbstractMvcEndpoint {
    private boolean sensitive = false;
    private boolean enabled = true;

    private Map<String, EndpointSource<?>> sources = Maps.newConcurrentMap();
    private Map<String, Object> EMPTY_VARS = ImmutableMap.of();
    public final EndpointSource<Object> NONE = new EndpointSource<Object>() {
        public Object getEndpointData(Map<String, Object> vars) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        public String getEndpointPath() { return ""; }
    };

    @Autowired
    private ApplicationContext context;

    // https://moelholm.com/2016/08/18/spring-boot-introduce-your-own-insight-endpoints/
    public SystemEndpoint() {
        super("/system", false, true);
    }

    @EventListener
    public void register(ApplicationReadyEvent event){
        Map<String, EndpointSource> beans = context.getBeansOfType(EndpointSource.class, false, true);
        beans.values().forEach(s -> sources.put(s.getEndpointPath(), s));
    }

    @ResponseBody
    @RequestMapping(value="/{path}/**", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object supports(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        path = StringUtils.substringAfter(path, "/system");
        return lookup(path).getEndpointData(EMPTY_VARS);
    }

    private EndpointSource<?> lookup(String path){
        return sources.getOrDefault(path, NONE);
    }

    /** interface for lookup */
    public interface EndpointSource<T> {
        public String getEndpointPath();
        public T getEndpointData(Map<String, Object> vars);
    }
}