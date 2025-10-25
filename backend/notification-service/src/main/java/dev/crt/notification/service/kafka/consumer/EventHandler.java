package dev.crt.notification.service.kafka.consumer;

import reactor.core.publisher.Mono;

public interface EventHandler {
    String getTopic();
    Mono<Void> handle (String json);
}
