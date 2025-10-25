package dev.crt.uploader.service.kafka.events;

import java.util.UUID;

public record VideoUploadFailedEvent(UUID creatorId, String status, String message) {
}
