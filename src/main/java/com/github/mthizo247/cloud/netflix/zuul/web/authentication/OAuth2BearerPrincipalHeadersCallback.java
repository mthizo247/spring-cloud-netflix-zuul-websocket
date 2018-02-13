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

import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;

/**
 * @author Marcin Podlodowski
 */
public class OAuth2BearerPrincipalHeadersCallback extends AbstractHeadersCallback {

    @Override
    protected void applyHeadersInternal(WebSocketSession userAgentSession, WebSocketHttpHeaders headers) {
        OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) userAgentSession.getPrincipal();
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) oAuth2Authentication.getDetails();
        String accessToken = details.getTokenValue();
        headers.put(HttpHeaders.AUTHORIZATION, Collections.singletonList("Bearer " + accessToken));
        if (logger.isDebugEnabled()) {
            logger.debug("Added Oauth2 bearer token authentication header for user " +
                    oAuth2Authentication.getName() + " to web sockets http headers");
        }
    }

    @Override
    protected boolean shouldApplyHeaders(WebSocketSession userAgentSession, WebSocketHttpHeaders headers) {
        return !headers.containsKey(HttpHeaders.AUTHORIZATION) && userAgentSession.getPrincipal() instanceof OAuth2Authentication;
    }
}
