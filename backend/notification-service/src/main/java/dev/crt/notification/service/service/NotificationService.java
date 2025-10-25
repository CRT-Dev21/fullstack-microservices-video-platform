package dev.crt.notification.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crt.notification.service.handler.NotificationWebSocketHandler;
import dev.crt.notification.service.kafka.events.VideoNotificationEvent;
import dev.crt.notification.service.kafka.events.VideoUploadFailedEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@Service
public class NotificationService {

    private final NotificationWebSocketHandler handler;
    private final ObjectMapper mapper = new ObjectMapper();

    public NotificationService(NotificationWebSocketHandler handler) {
        this.handler = handler;
    }

    public Mono<Void> sendProcessSuccessToFrontend(VideoNotificationEvent event) {
        return serializeAndSend(
                event.creatorId().toString(),
                Map.of("videoId", event.videoId(), "creatorId", event.creatorId(), "status", event.status(), "message", "Your video has been uploaded successfully!")
        );
    }

    public Mono<Void> sendFailedUploadToFrontend(VideoUploadFailedEvent event) {
        return serializeAndSend(
                event.creatorId().toString(),
                Map.of("creatorId", event.creatorId(), "status", event.status(), "message", "Your video could not be uploaded, please try again later.")
        );
    }

    private Mono<Void> serializeAndSend(String creatorId, Map<String, Object> data) {
        return Mono.fromCallable(() -> mapper.writeValueAsString(data))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(JsonProcessingException.class, e -> {
                    System.err.println("ERROR: Failed to serialize notification for " + creatorId + ": " + e.getMessage());
                    return Mono.empty();
                })
                .flatMap(json -> handler.sendNotification(creatorId, json));
    }
}
