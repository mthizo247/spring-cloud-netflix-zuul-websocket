/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.mthizo247.cloud.netflix.zuul.web.socket;

import com.github.mthizo247.cloud.netflix.zuul.web.proxytarget.ProxyTargetResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.util.Assert;
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
 * A {@link WebSocketHandlerDecorator} that adds web socket support to zuul reverse proxy.
 *
 * @author Ronald Mthombeni
 * @author Salman Noor
 */
public class ProxyWebSocketHandler extends WebSocketHandlerDecorator {
    private final Logger logger = LoggerFactory.getLogger(ProxyWebSocketHandler.class);
    private final WebSocketHttpHeadersCallback headersCallback;
    private final SimpMessagingTemplate messagingTemplate;
    private final ProxyTargetResolver proxyTargetResolver;
    private final ZuulWebSocketProperties zuulWebSocketProperties;
    private final WebSocketStompClient stompClient;
    private final Map<WebSocketSession, ProxyWebSocketConnectionManager> managers = new ConcurrentHashMap<>();
    private ErrorHandler errorHandler;

    public ProxyWebSocketHandler(WebSocketHandler delegate,
                                 WebSocketStompClient stompClient,
                                 WebSocketHttpHeadersCallback headersCallback,
                                 SimpMessagingTemplate messagingTemplate,
                                 ProxyTargetResolver proxyTargetResolver,
                                 ZuulWebSocketProperties zuulWebSocketProperties) {
        super(delegate);
        this.stompClient = stompClient;
        this.headersCallback = headersCallback;
        this.messagingTemplate = messagingTemplate;
        this.proxyTargetResolver = proxyTargetResolver;
        this.zuulWebSocketProperties = zuulWebSocketProperties;
    }

    public void errorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    private String getWebSocketServerPath(ZuulWebSocketProperties.WsBrokerage wsBrokerage,
                                          URI uri) {
        String path = uri.toString();
        if (path.contains(":")) {
            path = UriComponentsBuilder.fromUriString(path).build().getPath();
        }

        for (String endPoint : wsBrokerage.getEndPoints()) {
            if (PatternMatchUtils.simpleMatch(toPattern(endPoint), path + "/")) {
                return endPoint;
            }
        }

        return null;
    }

    private ZuulWebSocketProperties.WsBrokerage getWebSocketBrokarage(URI uri) {
        String path = uri.toString();
        if (path.contains(":")) {
            path = UriComponentsBuilder.fromUriString(path).build().getPath();
        }

        for (Map.Entry<String, ZuulWebSocketProperties.WsBrokerage> entry : zuulWebSocketProperties
                .getBrokerages().entrySet()) {
            ZuulWebSocketProperties.WsBrokerage wsBrokerage = entry.getValue();
            if (wsBrokerage.isEnabled()) {
                for (String endPoint : wsBrokerage.getEndPoints()) {
                    if (PatternMatchUtils.simpleMatch(toPattern(endPoint), path + "/")) {
                        return wsBrokerage;
                    }
                }
            }
        }

        return null;
    }

    private String toPattern(String path) {
        path = path.startsWith("/") ? "**" + path : "**/" + path;
        return path.endsWith("/") ? path + "**" : path + "/**";
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus)
            throws Exception {
        disconnectFromProxiedTarget(session);
        super.afterConnectionClosed(session, closeStatus);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message)
            throws Exception {
        super.handleMessage(session, message);
        handleMessageFromClient(session, message);
    }

    private void handleMessageFromClient(WebSocketSession session,
                                         WebSocketMessage<?> message) throws Exception {
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
            if (logger.isDebugEnabled()) {
                logger.debug("STOMP COMMAND " + accessor.getCommand()
                        + " was not explicitly handled");
            }
        }
    }

    private void connectToProxiedTarget(WebSocketSession session) {
        URI sessionUri = session.getUri();
        ZuulWebSocketProperties.WsBrokerage wsBrokerage = getWebSocketBrokarage(
                sessionUri);

        Assert.notNull(wsBrokerage, "wsBrokerage must not be null");

        String path = getWebSocketServerPath(wsBrokerage, sessionUri);
        Assert.notNull(path, "Web socket uri path must be null");

        URI routeTarget = proxyTargetResolver.resolveTarget(wsBrokerage);

        Assert.notNull(routeTarget, "routeTarget must not be null");

        String uri = ServletUriComponentsBuilder
                .fromUri(routeTarget)
                .path(path)
                .replaceQuery(sessionUri.getQuery())
                .toUriString();

        ProxyWebSocketConnectionManager connectionManager = new ProxyWebSocketConnectionManager(
                messagingTemplate, stompClient, session, headersCallback, uri);
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
                // nothing
            }
        }
    }

    private void unsubscribeFromProxiedTarget(WebSocketSession session,
                                              WebSocketMessageAccessor accessor) {
        ProxyWebSocketConnectionManager manager = managers.get(session);
        if (manager != null) {
            manager.unsubscribe(accessor.getDestination());
        }
    }

    private void sendMessageToProxiedTarget(WebSocketSession session,
                                            WebSocketMessageAccessor accessor) {
        ProxyWebSocketConnectionManager manager = managers.get(session);
        manager.sendMessage(accessor.getDestination(), accessor.getPayload());
    }

    private void subscribeToProxiedTarget(WebSocketSession session,
                                          WebSocketMessageAccessor accessor) throws Exception {
        ProxyWebSocketConnectionManager manager = managers.get(session);
        manager.subscribe(accessor.getDestination());
    }
}
