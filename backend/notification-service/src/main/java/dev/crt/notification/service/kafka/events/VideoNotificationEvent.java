package dev.crt.notification.service.kafka.events;

import java.util.UUID;

public record VideoNotificationEvent(UUID creatorId, UUID videoId, String status, String message){}
