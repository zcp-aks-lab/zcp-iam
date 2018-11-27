package com.skcc.cloudz.zcp.iam.common.config.websocket;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.squareup.okhttp.Call;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.util.Watch;
import io.kubernetes.client.util.Watch.Response;

public abstract class ResourceWatcher<T> implements Runnable {
    // https://github.com/kubernetes-client/java/issues/178#issuecomment-387602250
    // Watch.createWatch(client, call, watchType);
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    //TODO: detect change of secret and inject a new token
    protected Watch<T> watch;
    private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    public ResourceWatcher(ApiClient client) {
        try {
            CoreV1Api coreV1Api = new CoreV1Api(client);
            Call call = createWatchCall(coreV1Api);
            Type watchType = watchType();
            watch = Watch.createWatch(client, call, watchType);

            service.scheduleWithFixedDelay(this, 0, 5, TimeUnit.SECONDS);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    abstract public Call createWatchCall(CoreV1Api coreV1Api) throws ApiException;
    abstract public Type watchType();
    abstract public void forEach(T object, Response<T> res);

    public void run(){
        log.trace("{}", new Date());

        for(Response<T> res : watch) {
            try {
                forEach(res.object, res);
            } catch(Exception e){
                log.error("{}", e.getMessage());
                log.debug("", e);
            }
        }
    }
}