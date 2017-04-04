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

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.util.ErrorHandler;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.WebSocketStompClient;

/**
 * @author Ronald Mthombeni
 * @author Salman Noor
 */
public class ProxyWebSocketConnectionManagerTests {

	private ProxyWebSocketConnectionManager proxyConnectionManager;

	private SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
	private WebSocketStompClient stompClient = mock(WebSocketStompClient.class);
	private WebSocketSession wsSession = mock(WebSocketSession.class);
	private WebSocketHttpHeadersCallback headersCallback = mock(
			WebSocketHttpHeadersCallback.class);
	private StompSession serverSession = mock(StompSession.class);
	private ListenableFuture<StompSession> listenableFuture = (ListenableFuture<StompSession>) mock(
			ListenableFuture.class);
	private ErrorHandler errHandler = mock(ErrorHandler.class);

	@Before
	public void init() throws Exception {
		String uri = "http://example.com";
		proxyConnectionManager = new ProxyWebSocketConnectionManager(messagingTemplate,
				stompClient, wsSession, headersCallback, uri);

		proxyConnectionManager.errorHandler(errHandler);

		when(listenableFuture.get()).thenReturn(serverSession);
		when(stompClient.connect(uri, headersCallback.getWebSocketHttpHeaders(),
				proxyConnectionManager)).thenReturn(listenableFuture);
	}

	@Test
	public void sendStringMessageAsBytes() throws Exception {
		String destination = "/app/messages";
		String message = "hello";

		proxyConnectionManager.start();

		proxyConnectionManager.sendMessage(destination, message);

		verify(serverSession).send(destination, message.getBytes());
	}

	@Test
	public void handlesExcpetionUsingErrorHandler() throws Exception {
		StompHeaders headers = new StompHeaders();
		byte[] payload = new byte[0];
		Throwable exception = new Exception("E");
		proxyConnectionManager.handleException(serverSession, StompCommand.MESSAGE,
				headers, payload, exception);

		verify(errHandler).handleError(exception);
	}

	@Test
	public void handlesTransportErrorUsingErrorHandler() throws Exception {
		Throwable exception = new Exception("E");
		proxyConnectionManager.handleTransportError(serverSession, exception);

		verify(errHandler).handleError(exception);
	}
}