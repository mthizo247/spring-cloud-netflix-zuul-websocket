package org.springframework.cloud.netflix.zuul.web.socket;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * @author Ronald Mthombeni
 * @author Salman Noor
 */
@ConfigurationProperties("zuul.ws")
public class ZuulWebSocketProperties {
	private boolean enabled;
	private Map<String, WsBrokerage> brokerages = new HashMap<>();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Map<String, WsBrokerage> getBrokerages() {
		return brokerages;
	}

	public void setBrokerages(Map<String, WsBrokerage> brokerages) {
		this.brokerages = brokerages;
	}

	@PostConstruct
	public void init() {
		for (Map.Entry<String, WsBrokerage> entry : this.brokerages.entrySet()) {
			WsBrokerage wsBrokerage = entry.getValue();
			if (!StringUtils.hasText(wsBrokerage.getId())) {
				wsBrokerage.id = entry.getKey();
			}
		}
	}

	public static class WsBrokerage {
		private boolean enabled = true;
		private String id;
		private String[] endPoints;
		private String[] brokers;
		private String[] destinationPrefixes;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String[] getEndPoints() {
			return endPoints;
		}

		public void setEndPoints(String[] endPoints) {
			this.endPoints = endPoints;
		}

		public String[] getBrokers() {
			return brokers;
		}

		public void setBrokers(String[] brokers) {
			this.brokers = brokers;
		}

		public String[] getDestinationPrefixes() {
			return destinationPrefixes;
		}

		public void setDestinationPrefixes(String[] destinationPrefixes) {
			this.destinationPrefixes = destinationPrefixes;
		}
	}
}
