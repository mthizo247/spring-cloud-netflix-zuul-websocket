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
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

/**
 * Strategy to add different types of headers to web socket connections
 *
 * @author Ronald Mthombeni
 * @author Salman Noor
 */
public class CompositeHeadersCallback implements WebSocketHttpHeadersCallback {
    private List<WebSocketHttpHeadersCallback> headersCallbacks;

    public CompositeHeadersCallback(final List<WebSocketHttpHeadersCallback> headersCallbacks) {
        this.headersCallbacks = headersCallbacks;
    }


    @Override
    public void applyHeaders(WebSocketSession userAgentSession, WebSocketHttpHeaders headers) {
        for (WebSocketHttpHeadersCallback callback : headersCallbacks) {
            callback.applyHeaders(userAgentSession, headers);
        }
    }

    public List<WebSocketHttpHeadersCallback> getHeadersCallbacks() {
        return headersCallbacks;
    }
}
