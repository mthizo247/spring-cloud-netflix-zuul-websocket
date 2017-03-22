package org.springframework.cloud.netflix.zuul.web.socket;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by ronald22 on 10/03/2017.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ZuulWebSocketConfiguration.class)
public @interface EnableZuulWebSocket {
}
