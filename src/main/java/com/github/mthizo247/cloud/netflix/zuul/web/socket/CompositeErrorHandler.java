package com.github.mthizo247.cloud.netflix.zuul.web.socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * Created by ronald22 on 06/04/2017.
 */
public class CompositeErrorHandler implements ProxyWebSocketErrorHandler {
    protected final Log logger = LogFactory.getLog(getClass());
    private final List<ProxyWebSocketErrorHandler> errorHandlers;

    public CompositeErrorHandler(final List<ProxyWebSocketErrorHandler> handlers) {
        this.errorHandlers = handlers;
    }

    @Override
    public void handleError(Throwable t) {
        for (ProxyWebSocketErrorHandler handler : errorHandlers) {
            callErrorHandler(handler, t);
        }

    }

    private void callErrorHandler(ProxyWebSocketErrorHandler handler, Throwable t) {
        try {
            handler.handleError(t);
        } catch (Throwable e) {
            logger.error("Error executing error handler " + handler, e);
        }
    }
}
