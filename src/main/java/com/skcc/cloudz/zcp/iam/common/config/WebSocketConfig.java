package com.skcc.cloudz.zcp.iam.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Atomics;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ws.WebSocket;

import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.WebSockets;
import io.kubernetes.client.util.WebSockets.SocketListener;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    /*
     * Spring WebSocket Support (Docs)
     * https://docs.spring.io/spring/docs/5.0.0.BUILD-SNAPSHOT/spring-framework-reference/html/websocket.html#websocket-server-handler
     */

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        HandshakeInterceptor interceptors = new HttpSessionHandshakeInterceptor();
		registry.addHandler(relayHandler(), "/iam/shell")
                .addInterceptors(interceptors);
	}

	@Bean
	public WebSocketHandler relayHandler() {
		return new RelayHandler().init();
    }

    public class RelayHandler extends AbstractRelayHandler {
        protected void handleTextMessage(WebSocketSession in, TextMessage message) throws Exception {
            WebSocketSession out = getRelaySession(in);

            message = new TextMessage(message.getPayload());
            out.sendMessage(message);
        }

        protected void handleBinaryMessage(WebSocketSession in, BinaryMessage message) {
            try {
                WebSocketSession out = getRelaySession(in);

                String msg = new String(message.getPayload().array());
                message = new BinaryMessage(msg.getBytes());
                out.sendMessage(message);
            } catch (IOException e) {
				e.printStackTrace();
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

    /*
     * https://github.com/spring-projects/spring-framework/blob/master/spring-websocket/src/test/java/org/springframework/web/socket/client/standard/StandardWebSocketClientTests.java
     */
    public abstract class AbstractRelayHandler extends TextWebSocketHandler {
        private String RELAY_SESSION = "__relay_session__";
        private String server = "wss://169.56.69.242:26239";
        private String path = "/api/v1/namespaces/{ns}/pods/{pod}/exec";
        private String query = "container={con}&stdin=1&stdout=1&stderr=1&tty=1&command={shell}";
        private String protocol = "base64.channel.k8s.io";

        private ApiClient client;

        public AbstractRelayHandler init() {
            try {
                //String token = "eyJhbGciOiJSUzI1NiIsImtpZCI6IiJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJ6Y3Atc3lzdGVtIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6InpjcC1zeXN0ZW0tYWRtaW4tdG9rZW4tajJ4cmYiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiemNwLXN5c3RlbS1hZG1pbiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6Ijc0NmVjNTdiLWNkZTYtMTFlOC1hYmE5LTBhNjdlY2Q5YjM1ZCIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDp6Y3Atc3lzdGVtOnpjcC1zeXN0ZW0tYWRtaW4ifQ.Z_mSWuk2Gwp_8t5b0ngdz0ZDa7h1eDHpv3eZvnrRmwMzv3Bc2BU3q_rXNMWz0m_4jA9coHbjxGGjz6NqtdYWPtyk9ofqgdgdJlzqky55CGsqdKVdevmsOkqQkS-_hStfu28HhDADkVKa1pFRXqFPzPJo3zZDJw3LVGMivZ2D3absrDwK5bLDIj9seSYAAEYum-EFVfFwx8q-ogzZN8eAHn2aV0GoblkQas82PAnHH65Lf1u1YRvch35hpcB8pbLibpN3yDWi2-5vrneZgSxdTCwXu6-ijy9WZuZ5CVIsH3eKYI8igMZRV0nriR2DZ8-Y5YYJTXJyCiypRuloq27tzym_YE0ol-Gvgmd15QR_yUgv2lS9Rp1gbOuHa-GzHLQg_DNHBtNm9D4shOfkHRvbvoMcW-rQK4LCuaYx13jGqXDEf6KlUV4q86lYER2g7dqhgT68_5FC8kUEaR0so7HRS2Egb6AJoQ3Xb9RAJTsl6vJvjw9wrbCCO5Z5VTVaEgVkDR07_GPpNaVAigZX6ux7BpB2WqKPl1UYHxmkcB_FU8Spku1FImbdkrugRoS26CKWxAhhCXieexdtLaCMYY8IFMAJS-a-GzxVNmHLORIOEwoJL-KRChl_HVk8BFw8YrJhuHkpsryHDAh5CxS78qkKbhRwR6HU4AO7ohSlChH7X70";

                client = ClientBuilder.standard().build();
                //client.setApiKey(token);
                //client.setApiKeyPrefix("Bearer");

                return this;
            } catch(Exception e){
                throw new RuntimeException(e);
            }
        }

        protected WebSocketSession getRelaySession(WebSocketSession in) throws Exception {
            Object val = in.getAttributes().get(RELAY_SESSION);

            if(val instanceof WebSocketSession){
                WebSocketSession out = (WebSocketSession) val;
                if(out.isOpen())
                    return out;
            }

            // create connection
            WebSocketSession out = createSession();

            in.getAttributes().put(RELAY_SESSION, out);
            out.getAttributes().put(RELAY_SESSION, in);
            return out;
        }

        protected WebSocketSession createSession() throws Exception {
            Map<String, Object> vars = Maps.newHashMap();
            vars.put("ns", "console");
            vars.put("pod", "web-ssh");
            vars.put("con", "alpine");
            vars.put("shell", "sh");
            
            //String uri = UriComponentsBuilder.fromUriString(server)
            String uri = UriComponentsBuilder.newInstance()
                .path(path)
                .query(query)
                .buildAndExpand(vars)
                .toString();

            // Exec.exec(...)
            SocketListener listener = null;
            AbstractRelayHandler handler = this;
            final AtomicReference<WebSocket> _socket = Atomics.newReference();
            final WebSocketSession out = new EmptyWebSocketSession(){
                private Map<String, Object> attr = Maps.newHashMap();

                public boolean isOpen() {
                    return _socket.get() != null;
                }

                public Map<String, Object> getAttributes() {
                    return attr;
                }

                public void sendMessage(WebSocketMessage<?> message) throws IOException {
                    MediaType type;
                    byte[] payload;

                    if(message instanceof TextMessage){
                        type = WebSocket.TEXT;
                        payload = TextMessage.class.cast(message).getPayload().getBytes();
                    } else if(message instanceof BinaryMessage){
                        type = WebSocket.BINARY;
                        payload = BinaryMessage.class.cast(message).getPayload().array();
                    } else {
                        //TODO: ...
                        return;
                    }

                    if(!isOpen()){
                        return;
                    }

					RequestBody body = RequestBody.create(type, payload);
					_socket.get().sendMessage(body);
                }
            };
            
            listener = new SocketListener(){
                public void open(String protocol, WebSocket socket) {
                    _socket.set(socket);
                    System.out.println("k8s connected!!!");
                }
                public void close() {
                    _socket.set(null);
                }

                public void textMessage(Reader in) {
                    try {
                        String body = IOUtils.toString(in);
                        TextMessage message = new TextMessage(body);
                        handler.handleTextMessage(out, message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                public void bytesMessage(InputStream in) {
                    try {
                        byte[] body = IOUtils.toByteArray(in);
                        BinaryMessage message = new BinaryMessage(body);
                        handler.handleBinaryMessage(out, message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
			WebSockets.stream(uri, "GET", client, listener);

            return out;
        }
    }
}