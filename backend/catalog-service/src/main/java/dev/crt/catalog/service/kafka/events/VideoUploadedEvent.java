package dev.crt.catalog.service.kafka.events;

import java.util.UUID;

public record VideoUploadedEvent(
        UUID videoId,
        UUID creatorId,
        String title,
        String description,
        String thumbnailUrl,
        String originalUrl
) {}