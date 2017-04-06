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

import java.security.Principal;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;

import com.github.mthizo247.cloud.netflix.zuul.web.socket.WebSocketHttpHeadersCallback;

/**
 * @author Ronald Mthombeni
 */
public class BasicAuthPrincipalHeadersCallback implements WebSocketHttpHeadersCallback {
    protected final Log logger = LogFactory.getLog(getClass());
	public WebSocketHttpHeaders getWebSocketHttpHeaders(
			WebSocketSession userAgentSession) {
		WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
		Principal principal = userAgentSession.getPrincipal();
		if (principal != null) {
			Authentication authentication = (Authentication) principal;
			String usernameColonPwd = authentication.getName() + ":"
					+ authentication.getCredentials().toString();
			String encodedCredentials = new String(
					Base64.encode(usernameColonPwd.getBytes()));
			headers.put(HttpHeaders.AUTHORIZATION,
					Collections.singletonList("Basic " + encodedCredentials));
            if(logger.isDebugEnabled()) {
                logger.debug("Added basic authentication header for user " + principal.getName() + " to web sockets http headers");
            }
		}
        else {
            if(logger.isDebugEnabled()) {
                logger.debug("Skipped adding basic authentication header since user session principal is null");
            }
        }
		return headers;
	}
}
