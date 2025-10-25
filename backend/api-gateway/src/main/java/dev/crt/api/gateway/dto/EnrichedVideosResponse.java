package dev.crt.api.gateway.dto;

import java.util.List;

public record EnrichedVideosResponse(
        List<EnrichedVideoMetadata> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
