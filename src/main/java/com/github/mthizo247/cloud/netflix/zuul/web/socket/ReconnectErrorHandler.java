package com.github.mthizo247.cloud.netflix.zuul.web.socket;

import java.io.IOException;

import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.security.web.util.ThrowableAnalyzer;

/**
 * Created by ronald22 on 06/04/2017.
 */
public class ReconnectErrorHandler implements ProxyWebSocketErrorHandler {
	private static final long CONNECTION_LOST_RECONNECT_INTERVAL = 10000;
	private ThrowableAnalyzer throwableAnalyzer = new ThrowableAnalyzer();

	@Override
	public void handleError(Throwable t) {
		if (!(t instanceof ProxySessionException))
			return;

		ProxySessionException exception = (ProxySessionException) t;

		if (shouldAttemptToReconnect(exception)) {
			executeReconnectThread(exception.getConnectionManager());
		}
	}

	private boolean shouldAttemptToReconnect(ProxySessionException proxyException) {
		if (!isConnectionLost(proxyException))
			return false;

		if (!proxyException.getConnectionManager().isConnectedToUserAgent())
			return false;

		return true;
	}

	private boolean isConnectionLost(ProxySessionException proxyException) {
		Throwable cause = proxyException.getCause();
		Throwable[] causeChain = throwableAnalyzer.determineCauseChain(cause);
		Throwable throwable = throwableAnalyzer
				.getFirstThrowableOfType(ConnectionLostException.class, causeChain);

		if (throwable != null)
			return true;

		throwable = throwableAnalyzer.getFirstThrowableOfType(IOException.class,
				causeChain);

		return throwable != null
				&& throwable.getMessage().toLowerCase().contains(" refused ");
	}

	private void executeReconnectThread(
			final ProxyWebSocketConnectionManager clientConnectionManager) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				clientConnectionManager.reconnect(CONNECTION_LOST_RECONNECT_INTERVAL);
			}
		});
		t.setDaemon(true);
		t.start();
	}
}
