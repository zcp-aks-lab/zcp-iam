package com.skcc.cloudz.zcp.iam.common.config.websocket;

import java.util.concurrent.TimeUnit;

import com.skcc.cloudz.zcp.iam.common.config.WebSocketConfig.AbstractRelayHandler;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.Dispatcher;
import com.squareup.okhttp.Response;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.PodLogs;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.util.ClientBuilder;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okio.BufferedSource;

public class LogFollowHandler extends AbstractRelayHandler {
    private Attr PIPE = new Attr("__pipe__");

    private ApiClient client;
    private CoreV1Api coreApi;

    public LogFollowHandler() {
        try {
            client = ClientBuilder.standard().build();
            // https://github.com/square/okhttp/issues/1930#issue-111840160
            client.getHttpClient().setReadTimeout(0, TimeUnit.NANOSECONDS);

            // https://square.github.io/okhttp/3.x/okhttp/okhttp3/ConnectionPool.html
            int maxIdleConnections = 50;
            long keepAliveDuration = 5 * 60 * 1000; // 5 min;
            ConnectionPool pool = new ConnectionPool(maxIdleConnections, keepAliveDuration);
            client.getHttpClient().setConnectionPool(pool);

            Dispatcher dispather = client.getHttpClient().getDispatcher();
            dispather.setMaxRequestsPerHost(dispather.getMaxRequests());

            coreApi = new CoreV1Api(client);

            // if (log.isTraceEnabled())
            //     client.setDebugging(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void afterConnectionClosed(WebSocketSession in, CloseStatus status) throws Exception {
        Disposable disposable = PIPE.of(in);
        disposable.dispose();
    }

    /**
     * @see PodLogs#streamNamespacedPodLog
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession in) throws Exception {
        String namespace = getQueryParams(in, "ns");
        String name = getQueryParams(in, "name");
        String container = getQueryParams(in, "con");
        Boolean follow = Boolean.parseBoolean(getQueryParams(in, "follow"));
        Integer limitBytes = NumberUtils.createInteger(getQueryParams(in, "limit"));
        Boolean previous = Boolean.parseBoolean(getQueryParams(in, "previous"));
        Integer sinceSeconds = NumberUtils.createInteger(getQueryParams(in, "since"));
        Integer tailLines = NumberUtils.createInteger(getQueryParams(in, "tail"));
        boolean timestamps = Boolean.parseBoolean(getQueryParams(in, "timestamps"));

        Call call = coreApi.readNamespacedPodLogCall(name, namespace, container, follow, limitBytes, "false", previous,
                sinceSeconds, tailLines, timestamps, null, null);
        Response response = call.execute();
        if (!response.isSuccessful()) {
            throw new ApiException("Logs request failed: " + response.code());
        }

        //TODO: cleanup threads
        BufferedSource source = response.body().source();
        Disposable disposable = Flowable
            .interval(500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .subscribe(l -> {
                while( !source.exhausted() ){
                    String line = source.readUtf8Line();
                    // log.debug("!!!!! kubectl logs -- {}", line);
                    TextMessage message = new TextMessage(line);
                    in.sendMessage(message);
                }
            });
        
        PIPE.to(in, disposable);
    }

    @Override
    protected WebSocketSession createSession(WebSocketSession in) throws Exception {
        throw new UnsupportedOperationException();
    }
}