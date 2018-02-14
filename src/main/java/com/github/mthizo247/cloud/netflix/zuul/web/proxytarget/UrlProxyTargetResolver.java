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

package com.github.mthizo247.cloud.netflix.zuul.web.proxytarget;

import com.github.mthizo247.cloud.netflix.zuul.web.socket.ZuulWebSocketProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Strategy to resolve zuul proxy target from a load balanced client
 *
 * @author Ronald Mthombeni
 * @author Salman Noor
 */
public class UrlProxyTargetResolver extends AbstractProxyTargetResolver {
    public static final int DEFAULT_ORDER = EurekaProxyTargetResolver.DEFAULT_ORDER + 10;

    public UrlProxyTargetResolver(ZuulProperties zuulProperties) {
        super(zuulProperties);
        this.order = DEFAULT_ORDER;
    }

    @Override
    public URI resolveTarget(ZuulWebSocketProperties.WsBrokerage wsBrokerage) {
        ZuulProperties.ZuulRoute zuulRoute = resolveRoute(wsBrokerage);
        if (zuulRoute == null || StringUtils.isEmpty(zuulRoute.getUrl())) return null;

        try {
            return new URI(zuulRoute.getUrl());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
