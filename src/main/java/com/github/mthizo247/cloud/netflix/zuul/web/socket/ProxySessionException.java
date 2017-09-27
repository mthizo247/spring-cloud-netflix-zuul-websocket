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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ProxySessionException that = (ProxySessionException) o;

		if (connectionManager != null ? !connectionManager.equals(that.connectionManager) : that.connectionManager != null)
			return false;
		return session != null ? session.equals(that.session) : that.session == null;
	}

	@Override
	public int hashCode() {
		int result = connectionManager != null ? connectionManager.hashCode() : 0;
		result = 31 * result + (session != null ? session.hashCode() : 0);
		return result;
	}

}
