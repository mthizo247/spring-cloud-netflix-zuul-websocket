package org.springframework.cloud.netflix.zuul.web.socket;

import org.springframework.web.socket.WebSocketHttpHeaders;

/**
 * Created by ronald22 on 10/03/2017.
 */
public interface WebSocketHttpHeadersCallback {
    WebSocketHttpHeaders getWebSocketHttpHeaders();
}
