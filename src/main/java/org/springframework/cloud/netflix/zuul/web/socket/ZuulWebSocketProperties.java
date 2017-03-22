package org.springframework.cloud.netflix.zuul.web.socket;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created by ronald22 on 10/03/2017.
 */
@ConfigurationProperties("zuul.ws")
public class ZuulWebSocketProperties {
    private String[] endPoints;
    private String[] brokers;
    private String[] destinationPrefixes;

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
