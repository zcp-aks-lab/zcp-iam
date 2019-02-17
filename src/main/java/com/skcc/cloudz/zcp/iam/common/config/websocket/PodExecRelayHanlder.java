package com.skcc.cloudz.zcp.iam.common.config.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;
import com.skcc.cloudz.zcp.iam.common.config.WebSocketConfig.AbstractRelayHandler;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.Dispatcher;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketCall;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Pair;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.WebSockets;
import io.kubernetes.client.util.WebSockets.SocketListener;

public class PodExecRelayHanlder extends AbstractRelayHandler {
    private String protocol = "base64.channel.k8s.io";
    //private String server = "wss://169.56.69.242:26239";
    private String PATH = "/api/v1/namespaces/{ns}/pods/{pod}/exec";
    private String QUERY = "container={con}&stdin=1&stdout=1&stderr=1&tty=1&command={shell}";

    protected Attr POD_NAME = new Attr("__pod_name__");
    protected Attr POD_NAMESPACE = new Attr("__pod_namespace__");
    protected Attr POD_CONTAINER = new Attr("__pod_container__");

    protected Attr CLEAN_UP_MESSAGE = new Attr("__pod_cleanup_message__");

    protected ApiClient client;

    public PodExecRelayHanlder() {
        try {
            client = ClientBuilder.standard().build();
            //https://github.com/square/okhttp/issues/1930#issue-111840160
            client.getHttpClient().setReadTimeout(0, TimeUnit.NANOSECONDS);

            // https://square.github.io/okhttp/3.x/okhttp/okhttp3/ConnectionPool.html
            int maxIdleConnections = 50;
            long keepAliveDuration = 5 * 60 * 1000; // 5 min;
            ConnectionPool pool = new ConnectionPool(maxIdleConnections, keepAliveDuration);
            client.getHttpClient().setConnectionPool(pool);

            Dispatcher dispather = client.getHttpClient().getDispatcher();
            dispather.setMaxRequestsPerHost(dispather.getMaxRequests());

            if(log.isTraceEnabled())
                client.setDebugging(true);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    protected void sendSystemMessage(WebSocketSession in, String msg){
        try {
            if(in != null && in.isOpen())
                in.sendMessage(new TextMessage("system: " + msg + "\r\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected WebSocketSession createSession(WebSocketSession in) throws Exception {
        sendSystemMessage(in, "connecting...");

        Map<String, String> vars = getQueryParams(in);
        String podName = POD_NAME.of(in, vars.get("pod"));
        String container = POD_CONTAINER.of(in, vars.get("con"));
        vars.put("pod", podName);
        vars.put("con", container);
        
        //String uri = UriComponentsBuilder.fromUriString(server)
        String path = UriComponentsBuilder.newInstance()
            .path(PATH)
            .query(QUERY)
            .buildAndExpand(vars)
            .toString();

        // Exec.exec(...)
        ExecSession out = new ExecSession();
        out.id = podName + "-" + in.getId();
        out.handler = this;
        out.protocol = this.protocol;
        out.attr.put(DIRECTION.val(), DIRECTION_OUT.val());
        out.attr.put(RELAY_SESSION.val(), in);
        
        if(WebSockets.SPDY_3_1.equals(protocol)){
            //WebSockets.stream(path, "GET", client, out);
        } else if("base64.channel.k8s.io".equals(protocol)){
            // When need to change protocol (eg. base64.channel.k8s.io + websocket)
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put(WebSockets.STREAM_PROTOCOL_HEADER, "base64.channel.k8s.io");
            headers.put(HttpHeaders.CONNECTION, HttpHeaders.UPGRADE);
            headers.put(HttpHeaders.UPGRADE, "websocket");

            String[] localVarAuthNames = new String[] {"BearerToken"};
            ArrayList<Pair> params = new ArrayList<Pair>();
            HashMap<String, Object> form = Maps.newHashMap();

            Request request =
                client.buildRequest(
                    path,     // endpoint
                    "GET",    // method
                    params,   // queryParams
                    params,   // collectionQueryParams
                    null,     // body
                    headers,
                    form,
                    localVarAuthNames,
                    null);    // progress
            WebSocketCall.create(client.getHttpClient(), request).enqueue(new WebSockets.Listener(out){
                public void onOpen(WebSocket webSocket, Response response) {
                    out.res = response;
                    super.onOpen(webSocket, response);
                }
                public void onClose(int code, String reason) {
                    try {
                        // relay session is aleady closed.
                        if(!out.isOpen()) return;

                        Thread.currentThread().setName("OkHttp WebSocket Close Replay");
                        log.info("Pod exec connection is closed. ({} :: {})", code, reason);

                        if(code < 1000) code += 2000;
                        CloseStatus status = new CloseStatus(code, reason);
                        out.socket = null;
                        out.close(status);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                public void onFailure(IOException e, Response res) {
                    try {
                        int code = res.code();
                        String message = res.message();

                        Thread.currentThread().setName("OkHttp WebSocket Failure Replay");
                        log.info("Fail to create pod exec connection. ({} :: {})", code, message);

                        IOUtils.closeQuietly(res.body());
                        sendSystemMessage(in, "fail to connect pod. please check status of pod or authority.");

                        CloseStatus status = new CloseStatus(code + 2000, message);
                        out.close(status);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
        }

        return out;
    }

    public class ExecSession extends EmptyWebSocketSession implements SocketListener {
        public String protocol = WebSockets.SPDY_3_1;
        /*
         * for WebSocketSession (spring websocket)
         */
        private WebSocketHandler handler;
        private Map<String, Object> attr = Maps.newHashMap();
        private String id;
        private boolean open = false;

        public String getId() { return id; }
        public boolean isOpen() { return open; }
        public Map<String, Object> getAttributes() { return attr; }

        public void sendMessage(WebSocketMessage<?> message) throws IOException {
            MediaType type = WebSocket.TEXT;
            String payload = "";

            if(message instanceof TextMessage){
                payload = (String) message.getPayload();
            } else if(message instanceof BinaryMessage){
                ByteBuffer data = (ByteBuffer) message.getPayload();
                payload = new String(data.array());
            } else {
                //TODO: ...
                return;
            }

            if(this.socket == null){
                return;
            }

            payload = "0" + Base64.encodeBase64String(payload.getBytes());
            log.trace(">>> {}",new String(payload));

            RequestBody body = RequestBody.create(type, payload);
            socket.sendMessage(body);
        }

        public void close(CloseStatus status) throws IOException {
            try {
                if(this.open){
                    this.open = false;

                    List<String> cleanup = CLEAN_UP_MESSAGE.of(this);
                    if(cleanup != null){
                        for(String msg : cleanup){
                            this.sendMessage(new TextMessage(msg));
                        }
                    }

                    if(this.socket != null){
                        this.socket.close(0, "user connection is closed.");
                        this.socket = null;
                    }

                    log.info("Close exec connection of {}({}).", DIRECTION.of(this), this.getId());
                }
            } catch (Exception e) {
                log.info("kube exec connection is closed with error({} :: {}).", e.getMessage(), e.getClass().getSimpleName());
                log.trace("", e); 
            }

            try {
                this.handler.afterConnectionClosed(this, CloseStatus.NORMAL);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //TODO: send close message to browser.
        }

        /*
         * for SocketListener (kube-client util)
         */
        private WebSocket socket;
        private Response res;
        public void open(String protocol, WebSocket socket) {
            this.open = true;
            this.socket = socket;

            WebSocketSession in = RELAY_SESSION.of(this);
            sendSystemMessage(in, "web ssh is prepared.");
        }
        public void close() {
            // ambiguous call between SocketListener and WebSocketSession
            throw new IllegalStateException("Unsupported method.");
        }

        public void textMessage(Reader in) {
            try {
                String body = IOUtils.toString(in);
                byte[] buf = Base64.decodeBase64(body.substring(1));

                log.trace("<<< {}", body);
                TextMessage message = new TextMessage(new String(buf));
                handler.handleMessage(this, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void bytesMessage(InputStream in) {
            try {
                byte[] buf = IOUtils.toByteArray(in);
                String body = new String(buf).substring(1);

                log.trace("<<< {}", body);
                BinaryMessage message = new BinaryMessage(body.getBytes());
                handler.handleMessage(this, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class EmptyWebSocketSession implements WebSocketSession {
        public String getId() { return null; }
        public URI getUri() { return null; }
        public HttpHeaders getHandshakeHeaders() { return null; }
        public Map<String, Object> getAttributes() { return null; }
        public Principal getPrincipal() { return null; }
        public InetSocketAddress getLocalAddress() { return null; }
        public InetSocketAddress getRemoteAddress() { return null; }
        public String getAcceptedProtocol() { return null; }
        public void setTextMessageSizeLimit(int messageSizeLimit) { }
        public int getTextMessageSizeLimit() { return 0; }
        public void setBinaryMessageSizeLimit(int messageSizeLimit) { }
        public int getBinaryMessageSizeLimit() { return 0; }
        public List<WebSocketExtension> getExtensions() { return null; }
        public void sendMessage(WebSocketMessage<?> message) throws IOException { }
        public boolean isOpen() { return false; }
        public void close() throws IOException { }
		public void close(CloseStatus status) throws IOException { }
    }
}