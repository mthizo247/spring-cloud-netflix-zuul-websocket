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

import org.junit.Test;

/**
 * @author Ronald Mthombeni
 * @author Salman Noor
 */
public class WebSocketMessageAccessorTests {
	private WebSocketMessageAccessor accessor;

	@Test
	public void connect() throws Exception {
		accessor = WebSocketMessageAccessor.create("CONNECT\n"
				+ "accept-version:1.1,1.0\n" + "heart-beat:10000,10000\n" + "\n");

		assertThat(accessor.getCommand(), is("CONNECT"));
	}

	@Test
	public void subscribe() throws Exception {
		accessor = WebSocketMessageAccessor.create(
				"SUBSCRIBE\n" + "id:sub-0\n" + "destination:/topic/greetings\n" + "\n");

		assertThat(accessor.getCommand(), is("SUBSCRIBE"));
		assertThat(accessor.getDestination(), is("/topic/greetings"));
	}

	@Test
	public void send() throws Exception {
		accessor = WebSocketMessageAccessor.create("SEND\n" + "destination:/app/hello\n"
				+ "content-length:16\n" + "\n" + "{\"name\":\"hell9\"}");

		assertThat(accessor.getCommand(), is("SEND"));
		assertThat(accessor.getDestination(), is("/app/hello"));
		assertThat(accessor.getPayload(), is("{\"name\":\"hell9\"}"));
	}
}