package com.skcc.cloudz.zcp.iam.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.google.common.reflect.TypeToken;
import com.skcc.cloudz.zcp.iam.common.config.WebSocketConfig.AbstractRelayHandler;
import com.skcc.cloudz.zcp.iam.manager.KubeCoreManager;
import com.squareup.okhttp.Call;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.TextStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.Exec;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1ObjectReference;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1Secret;
import io.kubernetes.client.models.V1ServiceAccount;
import io.kubernetes.client.util.Watch;
import io.kubernetes.client.util.Watch.Response;
import io.kubernetes.client.util.Yaml;

public class WebSshHandler extends PodExecRelayHanlder {
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
        Map<String, String> vars = getQueryParams(in);

        try {
            String namespace = vars.get("ns");
            String podName = StrSubstitutor.replace("web-ssh-${username}", vars);

            HANDLER.to(in, this);
            POD_NAME.to(in, podName);

            watcher.register(podName, in);
            manager.getPod(namespace, podName);

            return super.createSession(in);
        } catch(ApiException e) {
            if(e.getCode() == 404){
                createPod(in);
                return EMPTY;
            }
            throw e;
        }

    }

    private V1Pod createPod(WebSocketSession in) throws ApiException, IOException {
        Map<String, String> vars = getQueryParams(in);

        String namespace = vars.get("ns");
        String username = vars.get("username");
        String saName = "zcp-system-sa-" + username;

        // add context variables
        vars.put("namespace", namespace);
        vars.put("name.suffix", username);

        V1ServiceAccount sa = manager.getServiceAccount("zcp-system", saName);
        V1ObjectReference ref = sa.getSecrets().get(0);
        V1Secret secret = manager.getSecret("zcp-system", ref.getName());
        byte[] token = secret.getData().get("token");
        vars.put("token", new String(token));

        // read config.xml template
        TextStringBuilder template = new TextStringBuilder();
        Resource resource = context.getResource(templatePath);
        Resources.asCharSource(resource.getURL(), Charset.forName("UTF-8")).copyTo(template);

        // create request
        new StringSubstitutor(vars).replaceIn(template);
        V1Pod spec = Yaml.loadAs(template.asReader(), V1Pod.class);
        String podName = spec.getMetadata().getName();

        POD_NAME.to(in, podName);
        return manager.createPod(namespace, spec);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession in, CloseStatus status) throws Exception {
        watcher.unregister(POD_NAME.of(in));
        super.afterConnectionClosed(in, status);
    }

    private ConnectionWatcher watcher = new ConnectionWatcher(); 
    private class ConnectionWatcher implements Runnable{
        // https://github.com/kubernetes-client/java/issues/178#issuecomment-387602250
        // Watch.createWatch(client, call, watchType);
        private final Logger log = LoggerFactory.getLogger(this.getClass());

        private Map<String, WebSocketSession> status = Maps.newConcurrentMap();

        private Watch<V1Pod> watch;
        private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

        public ConnectionWatcher() {
            try {
                CoreV1Api coreV1Api = new CoreV1Api(client);

                String labelSelector = "app=web-ssh";
                Call call = coreV1Api.listPodForAllNamespacesCall(null, null, null, labelSelector, null, null, null, null, Boolean.TRUE, null, null);
                Type watchType = new TypeToken<Watch.Response<V1Pod>>(){}.getType();
                watch = Watch.createWatch(client, call, watchType);

                service.scheduleWithFixedDelay(this, 0, 5, TimeUnit.SECONDS);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }

        public void run() {
            log.debug("%s WatchHandler Runnable", new Date());

            watch.forEach(res -> {
                V1Pod pod = res.object;
                String name = pod.getMetadata().getName();
                String status = pod.getStatus().getPhase();
                System.out.format("watchEvent=%s, name=%s, status=%s\n", res.type, name, status);

                connect(res, pod, name, status);
            });
        }

		/*
         * for creating OUT connection 
         */
        private void connect(Response<V1Pod> res, V1Pod pod, String name, String status) {
            try {
                // if("Completed".equals(status)){
                if("Succeeded".equals(status)){
                    manager.deletePod(pod.getMetadata().getNamespace(), name);
                }

                if("Running".equals(status)){
                    WebSocketSession in = lookup(name);
                    if(in == null){
                        //TODO: delete unused ssh pod
                        return;
                    }

                    AbstractRelayHandler handler = HANDLER.of(in);
                    handler.getRelaySession(in);
                }
            } catch (ApiException e) {
                log.error("{}", e.getMessage());
                log.debug("{}", e.getCode());
                log.debug("{}", e.getResponseBody());
            } catch (Exception e) {
                log.error("{}", e.getMessage());
                log.debug("", e);
            }
        }

        /*
         * for Connection Mapping
         */
        public WebSocketSession lookup(String name) {
            return status.get(name);
        }

        public void register(String name, WebSocketSession session) {
            status.put(name, session);
        }
        
        public WebSocketSession unregister(String name){
            if(name == null)
                return null;

            WebSocketSession in = status.remove(name);

            try {
                // TODO: delete unused ssh pod
                Process ps = new Exec(client).exec("console", name, "echo 'ERROR' > /status".split(" "), false);
                //System.out.println(IOUtils.toString(ps.getInputStream()));
                System.out.println(IOUtils.toString(ps.getErrorStream()));
            } catch (ApiException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return in;
        }
    }
}