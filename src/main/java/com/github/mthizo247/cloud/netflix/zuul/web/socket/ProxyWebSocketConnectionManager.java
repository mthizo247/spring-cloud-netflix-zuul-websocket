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

import java.lang.reflect.Type;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.util.ErrorHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.ConnectionManagerSupport;
import org.springframework.web.socket.messaging.WebSocketStompClient;

/**
 * A web socket connection manager bridge between client and backend server via zuul
 * reverse proxy
 *
 * @author Ronald Mthombeni
 * @author Salman Noor
 */
public class ProxyWebSocketConnectionManager extends ConnectionManagerSupport
		implements StompSessionHandler {
	private final WebSocketStompClient stompClient;
	private final WebSocketSession userAgentSession;
	private final WebSocketHttpHeadersCallback httpHeadersCallback;
	private StompSession serverSession;
	private Map<String, StompSession.Subscription> subscriptions = new ConcurrentHashMap<>();
	private ErrorHandler errorHandler;
	private SimpMessagingTemplate messagingTemplate;

	public ProxyWebSocketConnectionManager(SimpMessagingTemplate messagingTemplate,
			WebSocketStompClient stompClient, WebSocketSession userAgentSession,
			WebSocketHttpHeadersCallback httpHeadersCallback, String uri) {
		super(uri);
		this.messagingTemplate = messagingTemplate;
		this.stompClient = stompClient;
		this.userAgentSession = userAgentSession;
		this.httpHeadersCallback = httpHeadersCallback;
	}

	public void errorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	private WebSocketHttpHeaders buildWebSocketHttpHeaders() {
		if (httpHeadersCallback != null) {
			return httpHeadersCallback.getWebSocketHttpHeaders(userAgentSession);
		}
		return new WebSocketHttpHeaders();
	}

	@Override
	protected void openConnection() {
		connect();
	}

	private void connect() {
		try {
			serverSession = stompClient
					.connect(getUri().toString(), buildWebSocketHttpHeaders(), this)
					.get();
		}
		catch (Exception e) {
			logger.error("Error connecting to web socket uri " + getUri(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void closeConnection() throws Exception {
		if (isConnected()) {
			this.serverSession.disconnect();
		}
	}

	@Override
	protected boolean isConnected() {
		return (this.serverSession != null && this.serverSession.isConnected());
	}

	@Override
	public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
		if (logger.isDebugEnabled()) {
			logger.debug("Proxied target now connected " + session);
		}
	}

	@Override
	public void handleException(StompSession session, StompCommand command,
			StompHeaders headers, byte[] payload, Throwable ex) {
		if (errorHandler != null) {
			errorHandler.handleError(ex);
		}
	}

	@Override
	public void handleTransportError(StompSession session, Throwable ex) {
		if (errorHandler != null) {
			errorHandler.handleError(ex);
		}
	}

	@Override
	public Type getPayloadType(StompHeaders headers) {
		return Object.class;
	}

	public void sendMessage(final String destination, final Object msg) {
		if (msg instanceof String) { // in case of a json string to avoid double
										// converstion by the converters
			serverSession.send(destination, ((String) msg).getBytes());
			return;
		}

		serverSession.send(destination, msg);
	}

	@Override
	public void handleFrame(StompHeaders headers, Object payload) {
		if (headers.getDestination() != null) {
			String destination = headers.getDestination();
			if (logger.isDebugEnabled()) {
				logger.debug("Received " + payload + ", To " + headers.getDestination());
			}

			Principal principal = userAgentSession.getPrincipal();
            String userDestinationPrefix = messagingTemplate.getUserDestinationPrefix();
			if (principal != null && destination.startsWith(userDestinationPrefix)) {
                destination = destination.substring(userDestinationPrefix.length());

                destination = destination.startsWith("/") ? destination
                        : "/" + destination;

				messagingTemplate.convertAndSendToUser(principal.getName(), destination,
						payload, copyHeaders(headers.toSingleValueMap()));
			}
			else {
				messagingTemplate.convertAndSend(destination, payload,
						copyHeaders(headers.toSingleValueMap()));
			}
		}
	}

	private Map<String, Object> copyHeaders(Map<String, String> original) {
		Map<String, Object> copy = new HashMap<>();
		for (String key : original.keySet()) {
			copy.put(key, original.get(key));
		}

		return copy;
	}

	private void connectIfNecessary() {
		if (!isConnected()) {
			connect();
		}
	}

	public void subscribe(String destination) throws Exception {
		connectIfNecessary();
		StompSession.Subscription subscription = serverSession.subscribe(destination,
				this);
		subscriptions.put(destination, subscription);
	}

	public void unsubscribe(String destination) {
		StompSession.Subscription subscription = subscriptions.remove(destination);
		if (subscription != null) {
			connectIfNecessary();
			subscription.unsubscribe();
		}
	}

	public void disconnect() {
		try {
			closeConnection();
		}
		catch (Exception e) {
			// nothing
		}
	}
}
