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

package com.github.mthizo247.cloud.netflix.zuul.web.socket;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Strategy to resolve zuul properties from route service is using eaureka service discovery
 *
 * @author Ronald Mthombeni
 * @author Salman Noor
 */
public class EurekaPropertiesResolver implements ZuulPropertiesResolver {
    private DiscoveryClient discoveryClient;
    private ZuulProperties zuulProperties;

    public EurekaPropertiesResolver(DiscoveryClient discoveryClient, ZuulProperties zuulProperties) {
        this.discoveryClient = discoveryClient;
        this.zuulProperties = zuulProperties;
    }


    @Override
    public String getRouteHost(ZuulWebSocketProperties.WsBrokerage wsBrokerage) {
        ZuulProperties.ZuulRoute zuulRoute = zuulProperties.getRoutes().get(wsBrokerage.getId());
        if (zuulRoute == null || StringUtils.isEmpty(zuulRoute.getServiceId())) return null;

        List<ServiceInstance> instances = discoveryClient.getInstances(zuulRoute.getServiceId());
        ServiceInstance serviceInstance = instances.get(0);
        return serviceInstance.getUri().toString();
    }
}
