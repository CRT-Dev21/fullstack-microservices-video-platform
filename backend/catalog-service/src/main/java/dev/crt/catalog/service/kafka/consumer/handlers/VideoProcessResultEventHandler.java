package dev.crt.catalog.service.kafka.consumer.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crt.catalog.service.kafka.events.VideoProcessFailureEvent;
import dev.crt.catalog.service.kafka.events.VideoProcessSuccessEvent;
import dev.crt.catalog.service.service.CatalogService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class VideoProcessResultEventHandler {

    private final CatalogService service;
    private final ObjectMapper mapper;

    public VideoProcessResultEventHandler(CatalogService service, ObjectMapper mapper){
        this.service = service;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "video.process.result", groupId = "catalog-service-group")
    public void handleProcessingResult(String message) {
        try {
            JsonNode jsonNode = mapper.readTree(message);
            String status = jsonNode.get("status").asText();

            if ("SUCCESS".equals(status)) {
                VideoProcessSuccessEvent successEvent = mapper.readValue(message, VideoProcessSuccessEvent.class);
                service.videoProcessSuccess(successEvent);
            } else if ("FAILURE".equals(status)) {
                VideoProcessFailureEvent failureEvent = mapper.readValue(message, VideoProcessFailureEvent.class);
                service.videoProcessFailed(failureEvent);
            } else {
                System.err.println("Unknown message: " + status);
            }

        } catch (JsonProcessingException e) {
            System.err.println("Error deserializing JSON: " + e.getMessage());
        }
    }
}
