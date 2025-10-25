package dev.crt.catalog.service.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crt.catalog.service.kafka.events.VideoCatalogedEvent;
import dev.crt.catalog.service.kafka.events.VideoNotificationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaPublisher {
    private final KafkaTemplate<String, String> template;
    private final ObjectMapper mapper;

    public KafkaPublisher(ObjectMapper mapper, KafkaTemplate<String, String> template) {
        this.mapper = mapper;
        this.template = template;
    }

    public void sendVideoCatalogedEvent(VideoCatalogedEvent event){
        try {
            String json = mapper.writeValueAsString(event);
            template.send("video.cataloged.event", event.videoId().toString(), json);
        } catch (JsonProcessingException e) {
            System.err.println("Error mapping JSON: " + e.getMessage());
        }
    }

    public void sendVideoNotification(VideoNotificationEvent event){
        try {
            String json = mapper.writeValueAsString(event);
            template.send("video.notification.event", event.videoId().toString(), json);
        } catch (JsonProcessingException e) {
            System.err.println("Error mapping JSON: " + e.getMessage());
        }
    }
}
