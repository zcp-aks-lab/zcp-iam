package com.skcc.cloudz.zcp.iam.common.config;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.skcc.cloudz.zcp.iam.common.config.WebSocketConfig.AbstractRelayHandler;
import com.skcc.cloudz.zcp.iam.manager.KubeCoreManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1ObjectReference;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1Secret;
import io.kubernetes.client.models.V1ServiceAccount;
import io.kubernetes.client.util.Yaml;

public class WebSshHandler extends PodExecRelayHanlder {
    private static Attr HANDLER = new Attr("__handler__");
    private static Attr POD_READY = new Attr("__pod_is_ready__");

    //@Value("${jenkins.template.folder}")
    private String templatePath = "classpath:/ssh/pod.yaml";

    @Autowired
    private ApplicationContext context;
    
    @Autowired
    private KubeCoreManager manager;

    private final WebSocketSession EMPTY = new EmptyWebSocketSession(){
        private Map<String, Object> attr = Maps.newHashMap();
        public Map<String, Object> getAttributes() { return attr; }
    };

    @Override
    protected WebSocketSession createSession(WebSocketSession in) throws Exception {
        if(POD_READY.of(in) == Boolean.TRUE){
            return super.createSession(in);
        } else {
            HANDLER.to(in, this);

            V1Pod pod = createPod(in);
            String name = pod.getMetadata().getName();

            RelayWebSocketRegistry.register(name, in);
            return EMPTY;
        }
    }

    private V1Pod createPod(WebSocketSession in) throws ApiException, IOException {
        CharSequence query = in.getUri().getQuery();
        Map<String, String> vars = Splitter.on('&').trimResults().withKeyValueSeparator("=").split(query);
        vars = Maps.newHashMap(vars);
        String namespace = vars.get("ns");
        // add context variables
        vars.put("namespace", namespace);
        //vars.put("name.suffix", Integer.toString(RandomUtils.nextInt(0, 1000)) );
        vars.put("name.suffix", "10");

        V1ServiceAccount sa = manager.getServiceAccount("zcp-system", "zcp-system-sa-cloudzcp-admin");
        V1ObjectReference ref = sa.getSecrets().get(0);
        V1Secret secret = manager.getSecret("zcp-system", ref.getName());
        byte[] token = secret.getData().get("token");
        vars.put("token", new String(token));

        // read config.xml template
        StringBuffer template = new StringBuffer();
        Resource resource = context.getResource(templatePath);
        Resources.asCharSource(resource.getURL(), Charset.forName("UTF-8")).copyTo(template);

        // create request
        String yaml = StringSubstitutor.replace(template, vars);
        V1Pod spec = (V1Pod) Yaml.load(yaml);

        POD_NAME.to(in, spec.getMetadata().getName());
        
        try {
            return manager.createPod(namespace, spec);
        } catch(ApiException e){
            log.debug("{}", e.getCode());
            log.debug("{}", e.getResponseBody());

            if(e.getCode() == 409){
                return manager.readPod(namespace, spec.getMetadata().getName());
            }

            throw e;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession in, CloseStatus status) throws Exception {
        RelayWebSocketRegistry.unregister(POD_NAME.of(in));
        super.afterConnectionClosed(in, status);
    }

    @Controller
    public static class WebSocketRelayStatusController{
        @ResponseBody
        @RequestMapping("/iam/web-ssh/status/{podName}")
        private String sshStatus(@PathVariable String podName) throws Exception {
            WebSocketSession session = RelayWebSocketRegistry.lookup(podName);
            if(session != null){
                WebSshHandler handler = HANDLER.of(session);
                POD_READY.to(session, Boolean.TRUE);
                handler.getRelaySession(session);
                return "OK";
            }

            return "ERROR";
        }
    }

    public static class RelayWebSocketRegistry {
        private static Map<String, WebSocketSession> status = Maps.newConcurrentMap();

        public static WebSocketSession lookup(String name) {
            return status.get(name);
        }

        public static void register(String name, WebSocketSession session) {
            status.put(name, session);
        }
        
        public static WebSocketSession unregister(String name){
            return name == null ? null : status.remove(name);
        }
    }
}