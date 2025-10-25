package dev.crt.catalog.service.dto;

import dev.crt.catalog.service.domain.Video;

import java.util.Map;
import java.util.stream.Collectors;

public class VideoMapper {

    public static VideoMetadata ToMetadata (Video video){
        return new VideoMetadata(
                video.getId(),
                video.getCreatorId(),
                video.getTitle(),
                video.getDescription(),
                normalizePathForWeb(video.getThumbnailUrl()),
                video.getVideoUrls().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> normalizePathForWeb(e.getValue())
                        )),
                video.getDuration(),
                video.getStatus().toString());
    }

    private static String normalizePathForWeb(String path){
        if (path == null) return null;

        return path
                .replace("\\", "/")
                .replace(" ", "%20");
    }
}
