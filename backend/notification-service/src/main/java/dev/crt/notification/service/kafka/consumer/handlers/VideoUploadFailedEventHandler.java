package dev.crt.notification.service.kafka.consumer.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crt.notification.service.kafka.consumer.EventHandler;
import dev.crt.notification.service.kafka.events.VideoUploadFailedEvent;
import dev.crt.notification.service.service.NotificationService;
import reactor.core.publisher.Mono;

public class VideoUploadFailedEventHandler implements EventHandler {
    private final ObjectMapper mapper;
    private final NotificationService service;

    public VideoUploadFailedEventHandler(NotificationService service, ObjectMapper mapper){
        this.mapper = mapper;
        this.service = service;
    }
    @Override
    public String getTopic() {
        return "video.upload.failed.event";
    }

    @Override
    public Mono<Void> handle(String json) {
        try {
            VideoUploadFailedEvent event = mapper.readValue(json, VideoUploadFailedEvent.class);
            return service.sendFailedUploadToFrontend(event);
        } catch (JsonProcessingException e) {
            System.err.println("Error parsing JSON: " + json);
            return Mono.error(e);
        }
    }
}
