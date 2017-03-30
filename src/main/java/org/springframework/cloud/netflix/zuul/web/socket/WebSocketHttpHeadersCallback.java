package org.springframework.cloud.netflix.zuul.web.socket;

import org.springframework.web.socket.WebSocketHttpHeaders;

/**
 * Callback strategy to supply web socket headers to handshake requests.
 *
 * @author Ronald Mthombeni
 * @author Salman Noor
 */
public interface WebSocketHttpHeadersCallback {
	WebSocketHttpHeaders getWebSocketHttpHeaders();
}
