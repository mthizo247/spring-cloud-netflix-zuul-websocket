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

import java.net.URI;
import java.util.List;

/**
 * Strategy to resolve zuul proxy target using delegation pattern
 *
 * @author Ronald Mthombeni
 * @author Salman Noor
 */
public class CompositeProxyTargetResolver implements ProxyTargetResolver {
    private List<ProxyTargetResolver> targetResolvers;

    public CompositeProxyTargetResolver(final List<ProxyTargetResolver> targetResolvers) {
        this.targetResolvers = targetResolvers;
    }

    @Override
    public URI resolveTarget(ZuulWebSocketProperties.WsBrokerage wsBrokerage) {
        URI target = null;
        for (ProxyTargetResolver resolver : targetResolvers) {
            target = resolver.resolveTarget(wsBrokerage);
            if (target != null)
                break;
        }

        return target;
    }

    public List<ProxyTargetResolver> getTargetResolvers() {
        return targetResolvers;
    }
}
