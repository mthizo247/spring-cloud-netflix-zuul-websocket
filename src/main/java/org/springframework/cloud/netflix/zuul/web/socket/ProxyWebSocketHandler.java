package org.springframework.cloud.netflix.zuul.web.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.ErrorHandler;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ronald22 on 10/03/2017.
 */
public class ProxyWebSocketHandler extends WebSocketHandlerDecorator {
    private final Logger logger = LoggerFactory.getLogger(ProxyWebSocketHandler.class);
    private final WebSocketHttpHeadersCallback headersCallback;
    private final SimpMessagingTemplate messagingTemplate;
    private final ZuulPropertiesResolver zuulPropertiesResolver;
    private final ZuulWebSocketProperties zuulWebSocketProperties;
    private final WebSocketStompClient stompClient;
    private final Map<WebSocketSession, ProxyWebSocketConnectionManager> managers =
            new ConcurrentHashMap<>();
    private ErrorHandler errorHandler;

    public ProxyWebSocketHandler(WebSocketHandler delegate, WebSocketStompClient stompClient, WebSocketHttpHeadersCallback headersCallback, SimpMessagingTemplate messagingTemplate, ZuulPropertiesResolver zuulPropertiesResolver, ZuulWebSocketProperties zuulWebSocketProperties) {
        super(delegate);
        this.stompClient = stompClient;
        this.headersCallback = headersCallback;
        this.messagingTemplate = messagingTemplate;
        this.zuulPropertiesResolver = zuulPropertiesResolver;
        this.zuulWebSocketProperties = zuulWebSocketProperties;
    }

    public void errorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    private String getWebSocketServerPath(URI uri) {
        String path = uri.toString();
        if (path.contains(":")) {
            path = UriComponentsBuilder.fromUriString(path).build().getPath();
        }

        for (String endPoint : zuulWebSocketProperties.getEndPoints()) {
            if (PatternMatchUtils.simpleMatch(toPattern(endPoint), path + "/")) {
                return endPoint;
            }
        }

        return null;
    }

    private String toPattern(String path) {
        return path.endsWith("/") ? path + "**" : path + "/**";
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        disconnectFromProxiedTarget(session);
        super.afterConnectionClosed(session, closeStatus);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        super.handleMessage(session, message);
        handleMessageFromClient(session, message);
    }

    private void handleMessageFromClient(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        boolean handled = false;
        WebSocketMessageAccessor accessor = WebSocketMessageAccessor.create(message);
        if (StompCommand.SEND.toString().equalsIgnoreCase(accessor.getCommand())) {
            handled = true;
            sendMessageToProxiedTarget(session, accessor);
        }

        if (StompCommand.SUBSCRIBE.toString().equalsIgnoreCase(accessor.getCommand())) {
            handled = true;
            subscribeToProxiedTarget(session, accessor);
        }

        if (StompCommand.UNSUBSCRIBE.toString().equalsIgnoreCase(accessor.getCommand())) {
            handled = true;
            unsubscribeFromProxiedTarget(session, accessor);
        }

        if (StompCommand.CONNECT.toString().equalsIgnoreCase(accessor.getCommand())) {
            handled = true;
            connectToProxiedTarget(session);
        }

        if (!handled) {
            logger.warn("STOMP COMMAND " + accessor.getCommand() + " was not explicitly handled");
        } else {
            logger.warn("STOMP COMMAND " + accessor.getCommand() + " was handled");
        }
    }

    private void connectToProxiedTarget(WebSocketSession session) {
        String path = getWebSocketServerPath(session.getUri());

        String url = zuulPropertiesResolver.getRouteHost();
        String uri = ServletUriComponentsBuilder.fromHttpUrl(url)
                .path(path)
                .toUriString();
        ProxyWebSocketConnectionManager connectionManager = new ProxyWebSocketConnectionManager(messagingTemplate, stompClient, session, headersCallback, uri);
        connectionManager.errorHandler(this.errorHandler);
        managers.put(session, connectionManager);
        connectionManager.start();
    }

    private void disconnectFromProxiedTarget(WebSocketSession session) {
        disconnectProxyManager(managers.remove(session));
    }

    private void disconnectProxyManager(ProxyWebSocketConnectionManager proxyManager) {
        if (proxyManager != null) {
            try {
                proxyManager.disconnect();
            } catch (Throwable ignored) {
                //nothing
            }
        }
    }

    private void unsubscribeFromProxiedTarget(WebSocketSession session, WebSocketMessageAccessor accessor) {
        ProxyWebSocketConnectionManager manager = managers.get(session);
        if (manager != null) {
            manager.unsubscribe(accessor.getDestination());
        }
    }

    private void sendMessageToProxiedTarget(WebSocketSession session, WebSocketMessageAccessor accessor) {
        ProxyWebSocketConnectionManager manager = managers.get(session);
        manager.sendMessage(accessor.getDestination(), accessor.getPayload());
    }

    private void subscribeToProxiedTarget(WebSocketSession session, WebSocketMessageAccessor accessor) throws Exception {
        ProxyWebSocketConnectionManager manager = managers.get(session);
        manager.subscribe(accessor.getDestination());
    }

    public static class Builder {
        private SimpMessagingTemplate messagingTemplate;
        private ZuulPropertiesResolver zuulPropertiesResolver;
        private ZuulWebSocketProperties zuulWebSocketProperties;
        private int inboundMessageSizeLimit;
        private MessageConverter messageConverter;
        private TaskScheduler taskScheduler;

        public Object build(WebSocketHandler delegate) {
            return null;
        }

        public Builder withMessagingTemplate(SimpMessagingTemplate messagingTemplate) {
            this.messagingTemplate = messagingTemplate;
            return this;
        }

        public Builder withZuulPropertiesResolver(ZuulPropertiesResolver zuulPropertiesResolver) {
            this.zuulPropertiesResolver = zuulPropertiesResolver;
            return this;
        }

        public Builder withZuulWebSocketProperties(ZuulWebSocketProperties zuulWebSocketProperties) {
            this.zuulWebSocketProperties = zuulWebSocketProperties;
            return this;
        }

        public Builder withInboundMessageSizeLimit(int inboundMessageSizeLimit) {
            this.inboundMessageSizeLimit = inboundMessageSizeLimit;
            return this;
        }

        public Builder withMessageConverter(MessageConverter messageConverter) {
            this.messageConverter = messageConverter;
            return this;
        }

        public Builder withTaskScheduler(TaskScheduler taskScheduler) {
            this.taskScheduler = taskScheduler;
            return this;
        }

        public Builder withDefaultHeartbeat(TaskScheduler taskScheduler) {
            this.taskScheduler = taskScheduler;
            return this;
        }
    }
}
