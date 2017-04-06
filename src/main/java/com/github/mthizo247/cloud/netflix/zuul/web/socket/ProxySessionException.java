package com.github.mthizo247.cloud.netflix.zuul.web.socket;

import org.springframework.messaging.simp.stomp.StompSession;

/**
 * Created by ronald22 on 06/04/2017.
 */
public class ProxySessionException extends Exception {
	private final ProxyWebSocketConnectionManager connectionManager;
	private final StompSession session;

	public ProxySessionException(ProxyWebSocketConnectionManager connectionManager,
			StompSession session, Throwable cause) {
		super(cause);
		this.connectionManager = connectionManager;
		this.session = session;
	}

	public ProxyWebSocketConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public StompSession getSession() {
		return session;
	}
}
