package org.springframework.cloud.netflix.zuul.web.socket;

import java.lang.annotation.*;

import org.springframework.context.annotation.Import;

/**
 * Sets up a Zuul web socket configuration so that it can bridge web socket communication
 * to backend servers.
 *
 * @author Ronald Mthombeni
 * @author Salman Noor
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ZuulWebSocketConfiguration.class)
public @interface EnableZuulWebSocket {
}
