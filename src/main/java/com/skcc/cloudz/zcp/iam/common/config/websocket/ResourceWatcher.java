package com.skcc.cloudz.zcp.iam.common.config.websocket;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.squareup.okhttp.Call;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.util.Watch;
import io.kubernetes.client.util.Watch.Response;

public abstract class ResourceWatcher<T> implements Runnable {
    // https://github.com/kubernetes-client/java/issues/178#issuecomment-387602250
    // Watch.createWatch(client, call, watchType);
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    //TODO: detect change of secret and inject a new token
    protected Watch<T> watch;
    private Type watchType;
    private Type paramType;
    private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    public ResourceWatcher(ApiClient client) {
        try {
            CoreV1Api coreV1Api = new CoreV1Api(client);
            Call call = createWatchCall(coreV1Api);
            watchType = watchType();
            watch = Watch.createWatch(client, call, watchType);

            if(watchType instanceof ParameterizedType){
                paramType = ParameterizedType.class.cast(watchType).getActualTypeArguments()[0];
            }

            service.scheduleWithFixedDelay(this, 0, 5, TimeUnit.SECONDS);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    abstract public Call createWatchCall(CoreV1Api coreV1Api) throws ApiException;
    abstract public Type watchType();
    abstract public void forEach(T object, Response<T> res) throws Exception;

    public void run(){
        log.trace("Check watch events about {}", paramType.getTypeName());

        for(Response<T> res : watch) {
            try {
                if(log.isDebugEnabled()){
                    String type = "<unknown>";
                    String name = "<unknown>";
                    if(res.object != null){
                        Class<?> clazz = res.object.getClass();
                        type = clazz.getSimpleName();

                        Field metaF = ReflectionUtils.findField(clazz, "metadata", V1ObjectMeta.class);
                        ReflectionUtils.makeAccessible(metaF);
                        V1ObjectMeta meta = (V1ObjectMeta) ReflectionUtils.getField(metaF, res.object);
                        name = meta.getName();
                    }

                    log.debug("Catch resource change event. {}({}) is {}.", name, type, res.type);
                }

                forEach(res.object, res);
            } catch(Exception e){
                if(e instanceof ApiException){
                    ApiException ae = (ApiException) e;
                    if(ae.getCode() != 404){
                        log.error("Fail to handle watch event. [type={}, msg={}({})]", paramType.getTypeName(), ae.getMessage(), ae.getCode());
                        log.debug("Fail to handle watch event. [type={}, body]\n{}", paramType.getTypeName(), ae.getResponseBody());
                    }
                    return;
                }

                log.error("{}", e.getMessage());
                log.debug("", e);
            }
        }
    }
}