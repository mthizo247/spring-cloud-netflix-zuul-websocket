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

import com.github.mthizo247.cloud.netflix.zuul.web.socket.WebSocketHttpHeadersCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author Ronald Mthombeni
 */
public abstract class AbstractHeadersCallback implements WebSocketHttpHeadersCallback {
    protected final Log logger = LogFactory.getLog(getClass());

    @Override
    public void applyHeaders(
            WebSocketSession userAgentSession, WebSocketHttpHeaders headers) {
        if (shouldApplyHeaders(userAgentSession, headers)) {
            applyHeadersInternal(userAgentSession, headers);
        }
    }

    protected abstract void applyHeadersInternal(WebSocketSession userAgentSession, WebSocketHttpHeaders headers);

    protected abstract boolean shouldApplyHeaders(WebSocketSession userAgentSession, WebSocketHttpHeaders headers);
}
