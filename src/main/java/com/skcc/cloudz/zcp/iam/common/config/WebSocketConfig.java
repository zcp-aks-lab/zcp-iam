package com.skcc.cloudz.zcp.iam.common.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

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
		registry.addHandler(handler(), "/iam/shell")
                .addInterceptors(interceptors);
	}

	@Bean
	public WebSocketHandler handler() {
        //PodExecRelayHanlder.test();
        return new PodExecRelayHanlder();
    }

    /*
     * https://github.com/spring-projects/spring-framework/blob/master/spring-websocket/src/test/java/org/springframework/web/socket/client/standard/StandardWebSocketClientTests.java
     */
    public abstract static  class AbstractRelayHandler extends AbstractWebSocketHandler {
        protected static String RELAY_SESSION = "__relay_session__";
        protected static String DIRECTION = "__direction__";
        protected static String DIRECTION_IN  = "IN";
        protected static String DIRECTION_OUT = "OUT";

        protected void handleTextMessage(WebSocketSession in, TextMessage message) throws Exception {
            WebSocketSession out = getRelaySession(in);
            if(out == null){ return; }

            System.out.format("DEBUG :: %s -> %s >> %s\n", in.getAttributes().get(DIRECTION), out.getAttributes().get(DIRECTION), message.getPayload());

            message = new TextMessage(message.getPayload());
            out.sendMessage(message);
        }

        protected void handleBinaryMessage(WebSocketSession in, BinaryMessage message) {
            try {
                WebSocketSession out = getRelaySession(in);
                String msg = new String(message.getPayload().array());
                if(out == null){ return; }

                System.out.format("DEBUG :: %s -> %s >> %s\n", in.getAttributes().get(DIRECTION), out.getAttributes().get(DIRECTION), msg);

                message = new BinaryMessage(msg.getBytes());
                out.sendMessage(message);
            } catch (IOException e) {
				e.printStackTrace();
            } catch (Exception e) {
				e.printStackTrace();
			}
        }

        public void afterConnectionEstablished(WebSocketSession in) throws Exception {
            System.out.println("-------------------------------------------------------------");
            System.out.println(in.getId());
            System.out.println(in.getAttributes());
            getRelaySession(in);
        }

        public void afterConnectionClosed(WebSocketSession in, CloseStatus status) throws Exception {
            WebSocketSession out = getRelaySession(in);

            System.out.format("%s :: Close relay connection %s.\n", in.getAttributes().get(DIRECTION), out);
            if(out != null && out.isOpen()){
                out.close(status);
            }
        }

        protected WebSocketSession getRelaySession(WebSocketSession in) throws Exception {
            Object val = in.getAttributes().get(RELAY_SESSION);

            if(val instanceof WebSocketSession){
                WebSocketSession out = (WebSocketSession) val;
                if(out.isOpen())
                    return out;
            }

            if(DIRECTION_OUT.equals(in.getAttributes().get(DIRECTION))) {
                System.out.println("ERROR :: Only IN connection can be create relay connection.");
                return null;
            }

            // create connection
            WebSocketSession out = createSession(in);

            in.getAttributes().put(RELAY_SESSION, out);
            in.getAttributes().put(DIRECTION, DIRECTION_IN);
            out.getAttributes().put(RELAY_SESSION, in);
            out.getAttributes().put(DIRECTION, DIRECTION_OUT);
            return out;
        }

        abstract protected WebSocketSession createSession(WebSocketSession in) throws Exception;
    }
}