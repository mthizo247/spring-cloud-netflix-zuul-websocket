package org.springframework.cloud.netflix.zuul.web.socket;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.WebSocketStompClient;

/**
 * Created by ronald22 on 31/03/2017.
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

	@Before
	public void init() throws Exception {
		String uri = "http://example.com";
		proxyConnectionManager = new ProxyWebSocketConnectionManager(messagingTemplate,
				stompClient, wsSession, headersCallback, uri);

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
}