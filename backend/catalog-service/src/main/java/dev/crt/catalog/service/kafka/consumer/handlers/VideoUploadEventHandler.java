package dev.crt.catalog.service.kafka.consumer.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crt.catalog.service.kafka.events.VideoUploadedEvent;
import dev.crt.catalog.service.service.CatalogService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class VideoUploadEventHandler {

    private final CatalogService service;
    private final ObjectMapper mapper;

    public VideoUploadEventHandler(CatalogService service, ObjectMapper mapper){
        this.service = service;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "video.uploaded.event", groupId = "catalog-service-group")
    public void handleVideoCreatedEvent(String json){
        try {
            VideoUploadedEvent event = mapper.readValue(json, VideoUploadedEvent.class);
            service.registerVideo(event);
        } catch (JsonProcessingException e) {
            System.err.println("Error mapping JSON: " + e.getMessage());
        }
    }
}
