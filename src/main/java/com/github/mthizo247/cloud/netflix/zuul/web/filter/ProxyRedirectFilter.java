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

package com.github.mthizo247.cloud.netflix.zuul.web.filter;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UrlPathHelper;

import com.netflix.util.Pair;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

/**
 * @author Ronald Mthombeni
 */
public class ProxyRedirectFilter extends ZuulFilter {
	private static String REDIRECT_TO_URL = "REDIRECT_TO_URL_"
			+ ProxyRedirectFilter.class.getName();

	RouteLocator routeLocator;
	UrlPathHelper urlPathHelper;

	public ProxyRedirectFilter(RouteLocator routeLocator) {
		this(routeLocator, new UrlPathHelper());
	}

	public ProxyRedirectFilter(RouteLocator routeLocator, UrlPathHelper urlPathHelper) {
		this.routeLocator = routeLocator;
		this.urlPathHelper = urlPathHelper;
	}

	@Override
	public String filterType() {
		return "post";
	}

	@Override
	public int filterOrder() {
		return 1;
	}

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		boolean isRedirect = ctx.getResponseStatusCode() == 301 || ctx.getResponseStatusCode() == 302;
		if (!isRedirect)
			return false;

		boolean hasCorrectLocation = false;

		List<Pair<String, String>> zuulResponseHeaders = ctx.getZuulResponseHeaders();
		for (Pair<String, String> zuulResponseHeader : zuulResponseHeaders) {
			if ("Location".equalsIgnoreCase(zuulResponseHeader.first())) {
				HttpServletRequest request = ctx.getRequest();
				String path = urlPathHelper.getPathWithinApplication(request);
				Route route = routeLocator.getMatchingRoute(path);
				UriComponents redirectTo = ServletUriComponentsBuilder
						.fromHttpUrl(zuulResponseHeader.second()).build();
				UriComponents routeLocation = ServletUriComponentsBuilder
						.fromHttpUrl(route.getLocation()).build();

				if (redirectTo.getHost().equalsIgnoreCase(routeLocation.getHost())
						&& redirectTo.getPort() == routeLocation.getPort()) {
					String toLocation = ServletUriComponentsBuilder
							.fromHttpUrl(zuulResponseHeader.second())
							.host(request.getServerName())
							.port(request.getServerPort())
							.replacePath(
									buildRoutePath(route, zuulResponseHeader.second()))
							.build().toUriString();

					ctx.put(REDIRECT_TO_URL, toLocation);
					hasCorrectLocation = true;
					break;
				}
			}
		}

		return hasCorrectLocation;
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		List<Pair<String, String>> zuulResponseHeaders = ctx.getZuulResponseHeaders();
		for (Pair<String, String> zuulResponseHeader : zuulResponseHeaders) {
			if ("Location".equalsIgnoreCase(zuulResponseHeader.first())) {
				zuulResponseHeader.setSecond(ctx.get(REDIRECT_TO_URL).toString());
				break;
			}
		}
		return null;
	}

	private String buildRoutePath(Route route, String httpUrl) {
		String path = ServletUriComponentsBuilder.fromHttpUrl(httpUrl).build().getPath();

		return route.getPrefix() == null ? path : route.getPrefix() + path;
	}

}
