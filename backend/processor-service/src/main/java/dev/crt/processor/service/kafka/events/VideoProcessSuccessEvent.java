package dev.crt.processor.service.kafka.events;

import java.util.Map;
import java.util.UUID;

public record VideoProcessSuccessEvent(
        UUID videoId,
        String status,
        Map<String, String> resolutions
){}
