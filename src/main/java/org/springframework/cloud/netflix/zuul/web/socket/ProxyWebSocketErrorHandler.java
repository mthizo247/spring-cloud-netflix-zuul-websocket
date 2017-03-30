package org.springframework.cloud.netflix.zuul.web.socket;

import org.springframework.util.ErrorHandler;

/**
 * A simple tagging interface for pluggable web socket connection error handlers
 *
 * @author Ronald Mthombeni
 * @author Salman Noor
 */
public interface ProxyWebSocketErrorHandler extends ErrorHandler {
}
