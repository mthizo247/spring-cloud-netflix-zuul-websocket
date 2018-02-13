/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.mthizo247.cloud.netflix.zuul.web.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

/**
 * @author Ronald Mthombeni
 */
public class LoginCookieHeadersCallback extends AbstractHeadersCallback {
    protected final Log logger = LogFactory.getLog(getClass());

    @Override
    protected void applyHeadersInternal(WebSocketSession userAgentSession, WebSocketHttpHeaders headers) {
        List<String> sessionCookies = userAgentSession.getHandshakeHeaders().get(HttpHeaders.COOKIE);
        headers.put(HttpHeaders.COOKIE, sessionCookies);
        if (logger.isDebugEnabled()) {
            logger.debug("Added cookie authentication header to web sockets http headers");
        }
    }

    @Override
    protected boolean shouldApplyHeaders(WebSocketSession userAgentSession, WebSocketHttpHeaders headers) {
        return !headers.containsKey(HttpHeaders.COOKIE) && userAgentSession.getHandshakeHeaders().containsKey(HttpHeaders.COOKIE);
    }
}
