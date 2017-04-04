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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Ronald Mthombeni
 * @author Salman Noor
 */
public class ZuulWebSocketPropertiesTests {
	private ZuulWebSocketProperties properties;

	@Before
	public void setUp() throws Exception {
		properties = new ZuulWebSocketProperties();
	}

	@Test
	public void brokerageIdSet() throws Exception {
		ZuulWebSocketProperties.WsBrokerage brokerage = new ZuulWebSocketProperties.WsBrokerage();
		brokerage.setId("bar");
		Map<String, ZuulWebSocketProperties.WsBrokerage> brokerages = new HashMap<>();
		brokerages.put("foo", brokerage);

		properties.setBrokerages(brokerages);

		properties.init();

		assertThat(brokerage.getId(), is("bar"));
	}

	@Test
	public void brokerageIdNotSet() throws Exception {
		ZuulWebSocketProperties.WsBrokerage brokerage = new ZuulWebSocketProperties.WsBrokerage();
		Map<String, ZuulWebSocketProperties.WsBrokerage> brokerages = new HashMap<>();
		brokerages.put("foo", brokerage);

		properties.setBrokerages(brokerages);

		properties.init();

		assertThat(brokerage.getId(), is("foo"));
	}
}