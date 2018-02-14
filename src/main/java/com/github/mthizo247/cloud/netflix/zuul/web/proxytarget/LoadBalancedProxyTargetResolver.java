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
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.net.URI;

/**
 * Strategy to resolve zuul proxy target from a load balanced client
 *
 * @author Ronald Mthombeni
 * @author Salman Noor
 */
public class LoadBalancedProxyTargetResolver extends AbstractProxyTargetResolver {
    public static final int DEFAULT_ORDER = HIGHEST_PRECEDENCE + 10;

    private LoadBalancerClient loadBalancerClient;

    public LoadBalancedProxyTargetResolver(LoadBalancerClient loadBalancerClient, ZuulProperties zuulProperties) {
        super(zuulProperties);
        Assert.notNull(loadBalancerClient, "loadBalancerClient can't be null");
        this.loadBalancerClient = loadBalancerClient;
        this.order = DEFAULT_ORDER;
    }

    @Override
    public URI resolveTarget(ZuulWebSocketProperties.WsBrokerage wsBrokerage) {
        ZuulProperties.ZuulRoute zuulRoute = resolveRoute(wsBrokerage);
        if (zuulRoute == null || StringUtils.isEmpty(zuulRoute.getServiceId())) return null;

        ServiceInstance serviceInstance = loadBalancerClient.choose(zuulRoute.getServiceId());
        return serviceInstance != null ? resolveUri(serviceInstance) : null;
    }


}
