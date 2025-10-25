package dev.crt.catalog.service.kafka.events;

import java.util.UUID;

public record VideoProcessFailureEvent(
        UUID videoId,
        String status,
        String errorCode,
        String errorMessage
){}
