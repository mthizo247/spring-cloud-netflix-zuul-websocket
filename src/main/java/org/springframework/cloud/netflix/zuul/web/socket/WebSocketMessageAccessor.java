package org.springframework.cloud.netflix.zuul.web.socket;

import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketMessage;

/**
 * Created by ronald22 on 25/01/2017.
 */
public final class WebSocketMessageAccessor {
    private String[] messageComponents;

    private WebSocketMessageAccessor() {
    }

    public static WebSocketMessageAccessor create(WebSocketMessage<?> message) {
        String msgPayload = message.getPayload().toString();
        WebSocketMessageAccessor accessor = new WebSocketMessageAccessor();
        accessor.messageComponents = StringUtils.tokenizeToStringArray(msgPayload, "\n");
        return accessor;
    }

    public static WebSocketMessageAccessor create(String message) {
        WebSocketMessageAccessor accessor = new WebSocketMessageAccessor();
        accessor.messageComponents = StringUtils.tokenizeToStringArray(message, "\n");
        return accessor;
    }

    public String getCommand() {
        if (accessible())
            return messageComponents[0];

        return null;
    }

    private boolean accessible() {
        return messageComponents != null && messageComponents.length > 0;
    }

    public String getDestination() {
        return getHeader("destination");
    }

    public String getHeader(String header) {
        if (!accessible()) return null;

        header = header.endsWith(":") ? header : header + ":";
        if (accessible()) {
            for (String messageComponent : messageComponents) {
                int indx = messageComponent.indexOf(header);
                if (indx != -1) {
                    return messageComponent.substring(indx + header.length());
                }
            }
        }

        return null;
    }

    public String getPayload() {
        if (!accessible()) return null;

        return messageComponents[messageComponents.length - 1];
    }
}
