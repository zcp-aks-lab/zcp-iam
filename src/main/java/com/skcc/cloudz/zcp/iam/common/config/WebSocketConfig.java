package com.skcc.cloudz.zcp.iam.common.config;

import java.io.IOException;
import java.util.Map;

import com.skcc.cloudz.zcp.iam.common.config.websocket.LogFollowHandler;
import com.skcc.cloudz.zcp.iam.common.config.websocket.PodExecRelayHanlder;
import com.skcc.cloudz.zcp.iam.common.config.websocket.WebSshHandler4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
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
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
@EnableWebSocket
@EnableScheduling
public class WebSocketConfig implements WebSocketConfigurer {
    /*
     * Spring WebSocket Support (Docs)
     * https://docs.spring.io/spring/docs/5.0.0.BUILD-SNAPSHOT/spring-framework-reference/html/websocket.html#websocket-server-handler
     */

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        HandshakeInterceptor interceptors = new HttpSessionHandshakeInterceptor();
		registry.addHandler(execHandler(), "/iam/exec")
                .addInterceptors(interceptors)
                .addHandler(wshHandler(), "/iam/wsh")
                .addInterceptors(interceptors)
                .addHandler(logsHandler(), "/iam/logs")
                .addInterceptors(interceptors);
	}

	@Bean
	public WebSocketHandler execHandler() {
        return new PodExecRelayHanlder();
    }

	@Bean
	public WebSocketHandler wshHandler() {
        return new WebSshHandler4();
    }

	@Bean
	public WebSocketHandler logsHandler() {
        return new LogFollowHandler();
    }

    /*
     * https://github.com/spring-projects/spring-framework/blob/master/spring-websocket/src/test/java/org/springframework/web/socket/client/standard/StandardWebSocketClientTests.java
     */
    public abstract static  class AbstractRelayHandler extends AbstractWebSocketHandler {
        protected static final Attr RELAY_SESSION = new Attr("__relay_session__");
        protected static final Attr DIRECTION = new Attr("__direction__");
        protected static final Attr DIRECTION_IN = new Attr("IN");
        protected static final Attr DIRECTION_OUT = new Attr("OUT");
        protected static final Attr STATUS = new Attr("__status__");
        protected static final Attr STATUS_CLOSE = new Attr("CLOSE");
        protected static final Attr QUERY_PARAM = new Attr("__query_param_map__");
        protected static final Attr HANDLER = new Attr("__handler__");

        public static class Attr {
            private String val;
            public Attr(String val){ this.val = val; }
            public String val(){ return val; }
            public String toString(){ return val(); }
            public <T> T of(WebSocketSession session){
                if(session == null) return null;
                if(session.getAttributes() == null)
                    return null;

                return (T) session.getAttributes().get(this.val);
            }
            public <T> T of(WebSocketSession session, T _default){
                T obj = this.of(session);
                if(obj != null)
                    return obj;
                return this.to(session, _default);
            }

            public <T> T to(WebSocketSession session, T value){
                if(session != null)
                    session.getAttributes().put(this.val, value);
                
                return value;
            }
        }

        protected final Logger log = LoggerFactory.getLogger(this.getClass());

        @Override
        protected void handleTextMessage(WebSocketSession in, TextMessage message) throws Exception {
            WebSocketSession out = getRelaySession(in);
            if(out == null){ return; }

            log.trace("{} -> {} >>> {}", DIRECTION.of(in), DIRECTION.of(out), message.getPayload());

            message = new TextMessage(message.getPayload());
            out.sendMessage(message);
        }

        @Override
        protected void handleBinaryMessage(WebSocketSession in, BinaryMessage message) {
            try {
                WebSocketSession out = getRelaySession(in);

                if(log.isTraceEnabled()){
                    String msg = new String(message.getPayload().array());
                    log.trace("{} -> {} >>> {}", DIRECTION.of(in), DIRECTION.of(out), msg);
                }

                if(out == null){ return; }

                message = new BinaryMessage(message.getPayload());
                out.sendMessage(message);
            } catch (IOException e) {
				e.printStackTrace();
            } catch (Exception e) {
				e.printStackTrace();
			}
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession in) throws Exception {
            log.info("WebSocket {}({}) is connected.", DIRECTION.of(in, DIRECTION_IN), in.getId());
            log.trace("{}", in.getAttributes());
            getRelaySession(in);
        }

        @Override
        public void afterConnectionClosed(WebSocketSession in, CloseStatus status) throws Exception {
            log.info("{}({}) connection is closed.", DIRECTION.of(in), in.getId());
            STATUS.to(in, STATUS_CLOSE); 

            WebSocketSession out = RELAY_SESSION.of(in);
            if(!STATUS_CLOSE.equals(STATUS.of(out))){
                STATUS.to(out, STATUS_CLOSE);

                if(out.isOpen()){
                    log.debug("Try to close relay connection {}({}).", DIRECTION.of(out), out.getId());
                    out.close(status);
                }
            }
        }

        protected WebSocketSession getRelaySession(WebSocketSession in) throws Exception {
            Object val = RELAY_SESSION.of(in);

            if(val instanceof WebSocketSession){
                WebSocketSession out = (WebSocketSession) val;
                if(out.isOpen())
                    return out;
            }

            if(STATUS_CLOSE.equals(STATUS.of(in))) {
                log.debug("{}({}) is closing.", DIRECTION.of(in), in.getId());
                return null;
            }

            if(DIRECTION_OUT.equals(DIRECTION.of(in))) {
                log.debug("{}({}) is invalid direction. Skip creating relay.", DIRECTION.of(in), in.getId());
                return null;
            }

            // create connection
            WebSocketSession out = createSession(in);

            in.getAttributes().put(RELAY_SESSION.val(), out);
            in.getAttributes().put(DIRECTION.val(), DIRECTION_IN);
            out.getAttributes().put(RELAY_SESSION.val(), in);
            out.getAttributes().put(DIRECTION.val(), DIRECTION_OUT);
            return out;
        }

        protected Map<String, String> getQueryParams(WebSocketSession session){
            Map<String, String> vars = QUERY_PARAM.of(session);
            if(vars == null){
                vars = UriComponentsBuilder.fromUri(session.getUri())
                    .build()
                    .getQueryParams()
                    .toSingleValueMap();
                QUERY_PARAM.to(session, vars);
            }

            return vars;
        }

        protected String getQueryParams(WebSocketSession session, String key){
            Map<String, String> vars = getQueryParams(session);
            return vars.get(key);
        }

        abstract protected WebSocketSession createSession(WebSocketSession in) throws Exception;
    }
}