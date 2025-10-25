package dev.crt.catalog.service.dto;

import java.util.List;

public record VideosResponse (List<VideoMetadata> content, int page, int size, long totalElements, int totalPages){
}
