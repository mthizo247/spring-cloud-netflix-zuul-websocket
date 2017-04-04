# spring-cloud-netflix-zuul-websocket
A simple library to enable Zuul reverse proxy web socket support in spring applications.

## USAGE

spring-cloud-netflix-zuul-websocket is available from **Maven Central**

```xml
<dependency>
  <groupId>com.github.mthizo247</groupId>
  <artifactId>spring-cloud-netflix-zuul-websocket</artifactId>
  <version>1.0.0-RELEASE</version>
</dependency>
```

### Who is this for?

This is for anyone using **Spring Netflix Zuul** to proxy requests to back-ends which supports web sockets but the proxy layer does not.
**Netflix Zuul** does not natively support web sockets.

### How do I use this?

Enable it like so:

```java
@SpringBootApplication
@EnableZuulWebSocket
@EnableWebSocketMessageBroker
public class ProxyApplication {
	public static void main(String[] args) {
		SpringApplication.run(ProxyApplication.class, args);
	}
}
```

Then in your spring application properties(e.g application.yml)

```
server:
  port: 7078

zuul:
   routes:
    hello:
      path: /**
      url: http://localhost:7079
      customSensitiveHeaders: true
   ws:
      brokerages:
        hello:
          end-points: /ws
          brokers:  /topic
          destination-prefixes: /app
```

With this you should have web sockets to your back-end service working correctly.

**Checkout** this [demo](https://github.com/mthizo247/zuul-websocket-support-demo)
