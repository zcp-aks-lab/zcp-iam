package com.skcc.cloudz.zcp.iam.common.config.websocket;

import java.io.EOFException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.base.Predicates;
import com.google.common.collect.ForwardingTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.squareup.okhttp.Call;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.util.Watch;
import io.kubernetes.client.util.Watch.Response;

public class WebSocketUtils {
    /**
     * 
     */
    public static class MultimapTable<R, C, V> extends ForwardingTable<R,C,List<V>> {
        private List<V> NULL = ImmutableList.of();

        private Table<R, C, List<V>> del = HashBasedTable.create();

        private List<V> createList() {
            List<V> list = Lists.newArrayList();
            return list;
        }

        public static <R,C,V> MultimapTable<R,C,V> cretae(){
            return new MultimapTable<R,C,V>();
        }

        /*
        * for Forwarding
        */
        @Override
        protected Table<R, C, List<V>> delegate() {
            return del;
        }

        /*
        * for Table
        */
        @Override
        public List<V> get(Object rowKey, Object columnKey) {
            List<V> ret = super.get(rowKey, columnKey);
            return ret == null ? NULL : ret;
        }

        public int indexOf(R rowKey, C columnKey, V value) {
            List<V> list = this.get(rowKey, columnKey);
            return Iterables.indexOf(list, Predicates.equalTo(value));
        }

        public void putValue(R rowKey, C columnKey, V value) {
            List<V> list = super.get(rowKey, columnKey);

            if(list == null || NULL.equals(list)){
                list = createList();
                super.put(rowKey, columnKey, list);
            }

            if(!list.contains(value))
                list.add(value);
        }

        public boolean removeAll(R rowKey, V value) {
            if(!containsRow(rowKey))
                return false;

            boolean removed = false;
            Map<C, List<V>> row = row(rowKey);
            for(C columnKey : row.keySet()) {
                List<V> column = this.get(rowKey, columnKey);
                removed = column.remove(value) || removed;

                //if(column.isEmpty())
                //    this.put(rowKey, columnKey, NULL);
            }
            return removed;
        }
    }
    
    /**
     * 
     */
    public abstract static class ResourceWatcher<T> implements Runnable {
        // https://github.com/kubernetes-client/java/issues/178#issuecomment-387602250
        // Watch.createWatch(client, call, watchType);
        private final Logger log = LoggerFactory.getLogger(this.getClass());

        //TODO: detect change of secret and inject a new token
        private ApiClient client;
        private Call call;
        protected Watch<T> watch;
        private Type watchType;
        private Type paramType;
        private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

        public ResourceWatcher(ApiClient client) {
            try {
                this.client = client;
                CoreV1Api coreV1Api = new CoreV1Api(client);
                call = createWatchCall(coreV1Api);
                watchType = watchType();
                watch = Watch.createWatch(client, call, watchType);

                if(watchType instanceof ParameterizedType){
                    paramType = ParameterizedType.class.cast(watchType).getActualTypeArguments()[0];
                }
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }

            service.scheduleWithFixedDelay(this, 0, 5, TimeUnit.SECONDS);
        }

        abstract public Call createWatchCall(CoreV1Api coreV1Api) throws ApiException;
        abstract public Type watchType();
        abstract public void forEach(T object, Response<T> res) throws Exception;

        public void run(){
            try {
                log.trace("Check watch events about {}", paramType.getTypeName());

                if(watch == null){
                    log.info("Create new watch connection");
                    watch = Watch.createWatch(client, call, watchType);
                }

                for(Response<T> res : watch) {
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
                }
            } catch(Exception e){
                if(e instanceof ApiException){
                    ApiException ae = (ApiException) e;
                    if(ae.getCode() != 404){
                        log.error("Fail to handle watch event. [type={}, msg={}({})]", paramType.getTypeName(), ae.getMessage(), ae.getCode());
                        log.debug("Fail to handle watch event. [type={}, body]\n{}", paramType.getTypeName(), ae.getResponseBody());
                    }
                    return;
                }

                if(e instanceof RuntimeException && e.getCause() instanceof EOFException) {
                    IOUtils.closeQuietly(watch);
                    watch = null;
                }

                log.error("{}", e.getMessage());
                log.debug("", e);
            }
        }
    }
    
    /**
     * 
     */
    public static class TimeoutExecutorService implements Runnable {
        private static ExecutorService pool = Executors.newCachedThreadPool();

        protected Logger log = LoggerFactory.getLogger(this.getClass());

        protected long timeout = 5;
        protected TimeUnit unit = TimeUnit.SECONDS;
        protected Thread current;

        public void execute(){
            try {
                FutureTask<Void> future = new FutureTask(this, null);
                pool.execute(future);
                future.get(timeout, unit);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                onFailure(e);

                if(!current.interrupted()){
                    current.interrupt();
                }
            }
        }

        @Override
        public void run() {}

        protected void onFailure(Exception e){
            log.debug("", e);
            current.interrupt();
        }
    }
}