package dev.crt.notification.service.config;

import dev.crt.notification.service.handler.NotificationWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;

import java.util.Map;

@Configuration
public class WebSocketConfig {

    private final NotificationWebSocketHandler handler;

    public WebSocketConfig(NotificationWebSocketHandler handler) {
        this.handler = handler;
    }

    @Bean
    public SimpleUrlHandlerMapping webSocketMapping() {
        return new SimpleUrlHandlerMapping(Map.of(
                "/notifications", (WebSocketHandler) handler
        ), -1);
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
