package org.springframework.cloud.netflix.zuul.web.socket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ErrorHandler;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

/**
 * Zuul reverse proxy web socket configuration
 *
 * @author Ronald Mthombeni
 * @author Salman Noor
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(WebSocketHandler.class)
@ConditionalOnProperty(prefix = "zuul.ws", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(ZuulWebSocketProperties.class)
public class ZuulWebSocketConfiguration extends AbstractWebSocketMessageBrokerConfigurer {
	@Autowired
	ZuulWebSocketProperties zuulWebSocketProperties;
	@Autowired
	SimpMessagingTemplate messagingTemplate;
	@Autowired
	ZuulProperties zuulProperties;
	@Autowired
	ZuulPropertiesResolver zuulPropertiesResolver;
	@Autowired
	ProxyWebSocketErrorHandler proxyWebSocketErrorHandler;
	@Autowired
	WebSocketStompClient stompClient;
	@Autowired
	WebSocketHttpHeadersCallback webSocketHttpHeadersCallback;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		for (Map.Entry<String, ZuulWebSocketProperties.WsBrokerage> entry : zuulWebSocketProperties
				.getBrokerages().entrySet()) {
			ZuulWebSocketProperties.WsBrokerage wsBrokerage = entry.getValue();
			if (wsBrokerage.isEnabled()) {
				registry.addEndpoint(wsBrokerage.getEndPoints())
						// bypasses spring web security
						.setAllowedOrigins("*").withSockJS();
			}
		}
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		// prefix for subscribe
		for (Map.Entry<String, ZuulWebSocketProperties.WsBrokerage> entry : zuulWebSocketProperties
				.getBrokerages().entrySet()) {
			ZuulWebSocketProperties.WsBrokerage wsBrokerage = entry.getValue();
			if (wsBrokerage.isEnabled()) {
				config.enableSimpleBroker(
						mergeBrokersWithApplicationDestinationPrefixes(wsBrokerage));
				// prefix for send
				config.setApplicationDestinationPrefixes(
						wsBrokerage.getDestinationPrefixes());
			}
		}
	}

	private String[] mergeBrokersWithApplicationDestinationPrefixes(
			ZuulWebSocketProperties.WsBrokerage wsBrokerage) {
		List<String> brokers = new ArrayList<>(Arrays.asList(wsBrokerage.getBrokers()));

		for (String adp : wsBrokerage.getDestinationPrefixes()) {
			if (!brokers.contains(adp)) {
				brokers.add(adp);
			}
		}

		return brokers.toArray(new String[brokers.size()]);
	}

	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
		registration.addDecoratorFactory(handler -> {
			ProxyWebSocketHandler proxyWebSocketHandler = new ProxyWebSocketHandler(
					handler, stompClient, webSocketHttpHeadersCallback, messagingTemplate,
					zuulPropertiesResolver, zuulWebSocketProperties);
			proxyWebSocketHandler.errorHandler(proxyWebSocketErrorHandler);
			return proxyWebSocketHandler;
		});
	}

	@Bean
	@ConditionalOnMissingBean(WebSocketHttpHeadersCallback.class)
	public WebSocketHttpHeadersCallback webSocketHttpHeadersCallback() {
		return () -> new WebSocketHttpHeaders();
	}

	@Bean
	@ConditionalOnMissingBean
	public ZuulPropertiesResolver zuulPropertiesResolver(
			final ZuulProperties zuulProperties) {
		return (wsBrokerage) -> zuulProperties.getRoutes().get(wsBrokerage.getId()).getUrl();
	}

	@Bean
	@ConditionalOnMissingBean(WebSocketStompClient.class)
	public WebSocketStompClient stompClient(MessageConverter messageConverter,
			ThreadPoolTaskScheduler taskScheduler) {
		int bufferSizeLimit = 1024 * 1024 * 8;

		StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
		List<Transport> transports = new ArrayList<>();
		transports.add(new WebSocketTransport(webSocketClient));
		SockJsClient sockJsClient = new SockJsClient(transports);
		WebSocketStompClient client = new WebSocketStompClient(sockJsClient);
		client.setInboundMessageSizeLimit(bufferSizeLimit);
		client.setMessageConverter(messageConverter);
		client.setTaskScheduler(taskScheduler);
		client.setDefaultHeartbeat(new long[] { 0, 0 });
		return client;
	}

	@Bean
	@ConditionalOnMissingBean(TaskScheduler.class)
	public TaskScheduler stompClientTaskScheduler() {
		return new ThreadPoolTaskScheduler();
	}

	@Bean
	@ConditionalOnMissingBean(ProxyWebSocketErrorHandler.class)
	public ProxyWebSocketErrorHandler proxyWebSocketErrorHandler() {
		return new DefaultProxyWebSocketErrorHandler();
	}

	/**
	 * An {@link ErrorHandler} implementation that logs the Throwable at error level. It
	 * does not perform any additional error handling. This can be useful when suppression
	 * of errors is the intended behavior.
	 */
	private static class DefaultProxyWebSocketErrorHandler
			implements ProxyWebSocketErrorHandler {

		private final Log logger = LogFactory
				.getLog(DefaultProxyWebSocketErrorHandler.class);

		@Override
		public void handleError(Throwable t) {
			if (logger.isErrorEnabled()) {
				logger.error("Proxy web socket error occurred.", t);
			}
		}
	}
}
