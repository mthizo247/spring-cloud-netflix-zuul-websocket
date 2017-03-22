package org.springframework.cloud.netflix.zuul.web.socket;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.util.ErrorHandler;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.ConnectionManagerSupport;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ronald22 on 10/03/2017.
 */
public class ProxyWebSocketConnectionManager extends ConnectionManagerSupport implements StompSessionHandler {
    private final WebSocketStompClient stompClient;
    private final WebSocketSession userAgentSession;
    private final WebSocketHttpHeadersCallback httpHeadersCallback;
    private StompSession serverSession;
    private Map<String, StompSession.Subscription> subscriptions = new ConcurrentHashMap<String, StompSession.Subscription>();
    private ErrorHandler errorHandler;
    private SimpMessagingTemplate messagingTemplate;
    private final Object waitConnect = new Object();

    public ProxyWebSocketConnectionManager(SimpMessagingTemplate messagingTemplate, WebSocketStompClient stompClient, WebSocketSession userAgentSession, WebSocketHttpHeadersCallback httpHeadersCallback, String uri) {
        super(uri);
        this.messagingTemplate = messagingTemplate;
        this.stompClient = stompClient;
        this.userAgentSession = userAgentSession;
        this.httpHeadersCallback = httpHeadersCallback;
    }

    public void errorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    private WebSocketHttpHeaders buildWebSocketHttpHeaders() {
        if (httpHeadersCallback != null) {
            return httpHeadersCallback.getWebSocketHttpHeaders();
        }
        return new WebSocketHttpHeaders();
    }

    @Override
    protected void openConnection() {
        connect(0);
    }

    public void connect(long delay) {
        if (delay > 0) {
            logger.warn("Connection lost or refused, will attempt to reconnect after " + delay + " millis");
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                //
            }
        }
        stompClient.connect(getUri().toString(), buildWebSocketHttpHeaders(), this);
    }

    @Override
    protected void closeConnection() throws Exception {
        if (this.serverSession != null) {
            this.serverSession.disconnect();
        }

        if (userAgentSession != null) {
            userAgentSession.close();
        }
    }

    @Override
    protected boolean isConnected() {
        return (this.serverSession != null && this.serverSession.isConnected());
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        serverSession = session;

        notifyWaitConnect();
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable ex) {
        if (errorHandler != null) {
            errorHandler.handleError(ex);
        }

        notifyWaitConnect();
    }

    @Override
    public void handleTransportError(StompSession session, Throwable ex) {
        if (errorHandler != null) {
            errorHandler.handleError(ex);
        }

        notifyWaitConnect();
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return Object.class;
    }

    public void sendMessage(final String destination, final Object msg) {
        serverSession.send(destination, msg);
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        if (headers.getDestination() != null) {
            String destination = headers.getDestination();
            logger.info("Received " + payload + ", Foward " + headers.getDestination());
            Principal principal = userAgentSession.getPrincipal();
            if (principal != null) {
               // String trueDestination = destination.replace("/user", "");
                String name = principal.getName();
                messagingTemplate.convertAndSendToUser(name, destination, payload, copyHeaders(headers.toSingleValueMap()));
            } else {
                messagingTemplate.convertAndSend(destination, payload, copyHeaders(headers.toSingleValueMap()));
            }
        }
    }

    private Map<String, Object> copyHeaders(Map<String, String> original) {
        Map<String, Object> copy = new HashMap<>();
        for (String key : original.keySet()) {
            copy.put(key, original.get(key));
        }

        return copy;
    }

    private void notifyWaitConnect() {
        synchronized (waitConnect) {
            waitConnect.notifyAll();
        }
    }

    public void subscribe(String destination) throws Exception {
        if (serverSession == null || !serverSession.isConnected()) {
            connect(0);
            synchronized (waitConnect) {
                waitConnect.wait();
            }
        }

        StompSession.Subscription subscription = serverSession.subscribe(destination, this);
        subscriptions.put(destination, subscription);
    }

    public void unsubscribe(String destination) {
        StompSession.Subscription subscription = subscriptions.remove(destination);
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    public void disconnect() {
        try {
            if (isConnected())
                closeConnection();
        } catch (Exception e) {
            //nothing
        }
    }

    public void handlerProxiedTargetMessage(WebSocketSession session, WebSocketMessage<?> webSocketMessage) {
        final StompDecoder DECODER = new StompDecoder();
        BufferingStompDecoder bufferingDecoder = new BufferingStompDecoder(DECODER, 1024 * 1024 * 8);
        List<Message<byte[]>> messages = Collections.<Message<byte[]>>emptyList();
        ByteBuffer byteBuffer = null;
        if (webSocketMessage instanceof TextMessage) {
            byteBuffer = ByteBuffer.wrap(((TextMessage) webSocketMessage).asBytes());
        } else if (webSocketMessage instanceof BinaryMessage) {
            byteBuffer = ((BinaryMessage) webSocketMessage).getPayload();
        }

        if (byteBuffer != null) {
            messages = bufferingDecoder.decode(byteBuffer);
        }

        DefaultStompSession defaultStompSession
                = (DefaultStompSession) serverSession;

        for (Message<byte[]> message : messages) {
            defaultStompSession.handleMessage(message);
        }
    }
}
