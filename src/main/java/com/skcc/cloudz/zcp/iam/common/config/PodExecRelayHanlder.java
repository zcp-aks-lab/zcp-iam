package com.skcc.cloudz.zcp.iam.common.config;

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

import javax.annotation.PostConstruct;

import com.google.common.collect.Maps;
import com.skcc.cloudz.zcp.iam.common.config.WebSocketConfig.AbstractRelayHandler;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
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

    private ApiClient client;

    @PostConstruct
    public void init() {
        try {
            client = ClientBuilder.standard().build();
            //https://github.com/square/okhttp/issues/1930#issue-111840160
            client.getHttpClient().setReadTimeout(0, TimeUnit.NANOSECONDS);

            if(log.isTraceEnabled())
                client.setDebugging(true);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private void sendSystemMessage(WebSocketSession in, String msg){
        try {
            if(in != null)
                in.sendMessage(new TextMessage("system: " + msg + "\r\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected WebSocketSession createSession(WebSocketSession in) throws Exception {
        sendSystemMessage(in, "creating...");

        Map<String, Object> vars = Maps.newHashMap();
        vars.put("ns", "console");
        vars.put("pod", "web-ssh");
        vars.put("con", "alpine");
        vars.put("shell", "sh");
        
        //String uri = UriComponentsBuilder.fromUriString(server)
        String path = UriComponentsBuilder.newInstance()
            .path(PATH)
            .query(QUERY)
            .buildAndExpand(vars)
            .toString();

        // Exec.exec(...)
        ExecSession out = new ExecSession();
        out.id = vars.get("pod") + "-" + in.getId();
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
            });
        }

        return out;
    }


	public static void test() {
        try {
            PodExecRelayHanlder handler = new PodExecRelayHanlder(){
                protected void handleTextMessage(WebSocketSession in, TextMessage message) throws Exception {
                    log.trace("<<< {}", message.getPayload());
                }

                protected void handleBinaryMessage(WebSocketSession in, BinaryMessage message) {
                    try {
                        log.trace("<<< {}", new String(message.getPayload().array()) );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            handler.init();
            WebSocketSession out = handler.createSession(null);


            //case: SPDY
            //String body = "ls -al\r";
            //byte[] stream = new byte[]{ (byte) 0 };  //stdin
            //byte[] data = ArrayUtils.addAll(stream, body.getBytes());
            //BinaryMessage message = new BinaryMessage(data);
            //out.sendMessage(message);

            // https://github.com/kubernetes-client/java/blob/master/util/src/test/java/io/kubernetes/client/ExecTest.java#L74
            ByteBuffer data = ByteBuffer.allocate(10);
            data.putInt(0).put("ls ".getBytes());
            out.sendMessage(new BinaryMessage(data));

            data.clear();
            data.putInt(0).put("-al \r".getBytes());
            out.sendMessage(new BinaryMessage(data));

            //case: Base64
            //String body = "ls -al\r";
            //String payload = "0" + Base64.encodeBase64String(body.getBytes());
            //TextMessage message = new TextMessage(payload);
            //out.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ExecSession extends EmptyWebSocketSession implements SocketListener {
        public String protocol = WebSockets.SPDY_3_1;
        /*
         * for WebSocketSession (spring websocket)
         */
        private WebSocketHandler handler;
        private Map<String, Object> attr = Maps.newHashMap();
        private String id;

        public String getId() { return id; }
        public boolean isOpen() { return socket != null; }
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


            if(!isOpen()){
                //TODO: ...
                return;
            }

            payload = "0" + Base64.encodeBase64String(payload.getBytes());
            log.trace(">>> {}",new String(payload));

            RequestBody body = RequestBody.create(type, payload);
            socket.sendMessage(body);
        }

        public void close(CloseStatus status) throws IOException {
            try {
                if(this.socket != null){
                    WebSocket socket = this.socket;
                    this.socket = null;

                    socket.close(0, "....");

                    log.info("Close exec connection of {}({}).", DIRECTION.of(this), this.getId());
                }
            } catch (IOException | IllegalStateException e) {
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
        public void open(String protocol, WebSocket socket) {
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