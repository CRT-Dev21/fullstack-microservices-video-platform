package dev.crt.notification.service.kafka.events;

import java.util.UUID;

public record VideoUploadFailedEvent(UUID creatorId, String status, String message) {
}
