package dev.crt.api.gateway.dto;

import java.util.Map;
import java.util.UUID;

public record VideoMetadata(UUID videoId, UUID creatorId, String title, String description, String thumbnailUrl, Map<String, String> videoUrls, String duration, String status) {
}
