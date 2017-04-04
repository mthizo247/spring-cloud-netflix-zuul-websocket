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

import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketMessage;

/**
 * An accessor to acess web socket messages before discpatching them to the backend
 * server.
 *
 * @author Ronald Mthombeni
 * @author Salman Noor
 */
public final class WebSocketMessageAccessor {
	private String[] messageComponents;

	private WebSocketMessageAccessor() {
	}

	public static WebSocketMessageAccessor create(WebSocketMessage<?> message) {
		String msgPayload = message.getPayload().toString();
		return create(msgPayload);
	}

	public static WebSocketMessageAccessor create(String message) {
		WebSocketMessageAccessor accessor = new WebSocketMessageAccessor();
		accessor.messageComponents = StringUtils.tokenizeToStringArray(message, "\n");
		return accessor;
	}

	public String getCommand() {
		if (accessible())
			return messageComponents[0];

		return null;
	}

	private boolean accessible() {
		return messageComponents != null && messageComponents.length > 0;
	}

	public String getDestination() {
		return getHeader("destination");
	}

	public String getHeader(String header) {
		if (!accessible())
			return null;

		header = header.endsWith(":") ? header : header + ":";
		if (accessible()) {
			for (String messageComponent : messageComponents) {
				int indx = messageComponent.indexOf(header);
				if (indx != -1) {
					return messageComponent.substring(indx + header.length());
				}
			}
		}

		return null;
	}

	public String getPayload() {
		if (!accessible())
			return null;

		return messageComponents[messageComponents.length - 1];
	}
}
