package dev.crt.notification.service.kafka.consumer.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crt.notification.service.kafka.consumer.EventHandler;
import dev.crt.notification.service.kafka.events.VideoNotificationEvent;
import dev.crt.notification.service.service.NotificationService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class VideoNotificationEventHandler implements EventHandler {
    private final ObjectMapper mapper;
    private final NotificationService service;

    public VideoNotificationEventHandler(ObjectMapper mapper, NotificationService service) {
        this.mapper = mapper;
        this.service = service;
    }

    @Override
    public String getTopic() {
        return "video.notification.event";
    }

    @Override
    public Mono<Void> handle(String json) {
        try{
            VideoNotificationEvent event = mapper.readValue(json, VideoNotificationEvent.class);
            return service.sendProcessSuccessToFrontend(event);
        } catch (JsonProcessingException e){
            System.err.println("Error parsing JSON: "+json);
            return Mono.error(e);
        }
    }
}
