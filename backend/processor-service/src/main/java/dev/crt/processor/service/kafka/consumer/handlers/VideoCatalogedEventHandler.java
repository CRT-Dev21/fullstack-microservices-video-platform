package dev.crt.processor.service.kafka.consumer.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.crt.processor.service.kafka.consumer.EventHandler;
import dev.crt.processor.service.kafka.events.VideoCatalogedEvent;
import dev.crt.processor.service.service.VideoProcessor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class VideoCatalogedEventHandler implements EventHandler {
    private final ObjectMapper mapper;
    private final VideoProcessor processor;

    public VideoCatalogedEventHandler(ObjectMapper mapper, VideoProcessor processor) {
        this.mapper = mapper;
        this.processor = processor;
    }


    @Override
    public String getTopic() {
        return "video.cataloged.event";
    }

    @Override
    public Mono<Void> handle(String json) {
        VideoCatalogedEvent event;
        try {
            event = mapper.readValue(json, VideoCatalogedEvent.class);
            System.out.println(event.toString());
        } catch (Exception e){
            return Mono.error(new RuntimeException("Failed to read VideoCatalogedEvent", e));
        }

        return processor.processVideo(event.videoId(), event.videoUrl())
                .flatMap(videoUrls -> processor.publishProcessingSuccess(event.videoId(), videoUrls))
                .onErrorResume(e -> {
                    String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error during transcoding.";

                    System.err.println("Error processing video " + event.videoId() + ": " + errorMessage);

                    return processor.publishProcessingFailure(event.videoId(), "TRANSCODING_ERROR", errorMessage);
                });
    }
}
