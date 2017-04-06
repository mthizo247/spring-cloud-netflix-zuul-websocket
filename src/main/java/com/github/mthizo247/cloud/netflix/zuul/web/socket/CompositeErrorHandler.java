package com.github.mthizo247.cloud.netflix.zuul.web.socket;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by ronald22 on 06/04/2017.
 */
public class CompositeErrorHandler implements ProxyWebSocketErrorHandler {
	protected final Log logger = LogFactory.getLog(getClass());
	private final Set<ProxyWebSocketErrorHandler> handlers;

	public CompositeErrorHandler(final Set<ProxyWebSocketErrorHandler> handlers) {
		this.handlers = handlers;
	}

	@Override
	public void handleError(Throwable t) {
		for (ProxyWebSocketErrorHandler handler : handlers) {
			callErrorHandler(handler, t);
		}

	}

	private void callErrorHandler(ProxyWebSocketErrorHandler handler, Throwable t) {
		try {
			handler.handleError(t);
		}
		catch (Throwable e) {
			logger.error("Error executing error handler " + handler, e);
		}
	}
}
