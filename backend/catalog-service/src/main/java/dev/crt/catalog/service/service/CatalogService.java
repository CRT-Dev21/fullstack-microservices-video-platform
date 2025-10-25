package dev.crt.catalog.service.service;

import dev.crt.catalog.service.domain.Video;
import dev.crt.catalog.service.domain.VideoStatus;
import dev.crt.catalog.service.dto.VideosResponse;
import dev.crt.catalog.service.exception.VideoNotFoundException;
import dev.crt.catalog.service.kafka.events.*;
import dev.crt.catalog.service.dto.VideoMapper;
import dev.crt.catalog.service.dto.VideoMetadata;
import dev.crt.catalog.service.kafka.producer.KafkaPublisher;
import dev.crt.catalog.service.repository.VideoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CatalogService {

    private final VideoRepository repository;
    private final KafkaPublisher kafkaPublisher;

    private final Set<String> STOP_WORDS = Set.of(
            "the", "a", "an", "is", "of", "and", "or", "in", "with"
    );

    public CatalogService(VideoRepository videoRepository, KafkaPublisher kafkaPublisher){
        this.repository = videoRepository;
        this.kafkaPublisher = kafkaPublisher;
    }

    public VideosResponse getVideosOrSearch(String query, int page, int size) {
        if (query == null || query.isBlank()) {
            return getVideos(page, size);
        } else {
            return searchVideos(query, page, size);
        }
    }

    public VideosResponse getVideosByCreatorIdForOwner(UUID creatorId, int page, int size){
        Page<VideoMetadata> result = repository.findByCreatorIdOrderByCreatedAtDesc(creatorId, PageRequest.of(page, size))
                .map(VideoMapper::ToMetadata);

        return new VideosResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public VideosResponse getVideosByCreatorIdForPublic(UUID creatorId, int page, int size){
        Page<VideoMetadata> result = repository.findByCreatorIdAndStatusOrderByCreatedAtDesc(creatorId, VideoStatus.READY, PageRequest.of(page, size))
                .map(VideoMapper::ToMetadata);

        return new VideosResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public VideosResponse getRelatedVideos(String currentVideoTitle, UUID currentVideoId, int limit) {

        List<String> relevantKeywords = Arrays.stream(currentVideoTitle
                        .toLowerCase()
                        .replaceAll("[^a-z0-9\\s]", " ")
                        .split("\\s+"))
                .filter(word -> word.length() > 2)
                .filter(word -> !STOP_WORDS.contains(word))
                .distinct()
                .collect(Collectors.toList());

        String combinedQuery = String.join(" | ", relevantKeywords);

        PageRequest pageable = PageRequest.of(0, limit);

        Page<VideoMetadata> relatedResult = repository
                .findRelatedByFTS(
                        combinedQuery,
                        currentVideoId,
                        VideoStatus.READY.name(),
                        pageable
                )
                .map(VideoMapper::ToMetadata);

        return new VideosResponse(
                relatedResult.getContent(),
                relatedResult.getNumber(),
                relatedResult.getSize(),
                relatedResult.getTotalElements(),
                relatedResult.getTotalPages()
        );
    }

    private VideosResponse getVideos(int page, int size){
        Page<VideoMetadata> result = repository.findByStatus(VideoStatus.READY, PageRequest.of(page, size))
                .map(VideoMapper::ToMetadata);

        return new VideosResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private VideosResponse searchVideos(String query, int page, int size) {
        Page<VideoMetadata> result = repository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndStatus(
                        query, query, VideoStatus.READY, PageRequest.of(page, size))
                .map(VideoMapper::ToMetadata);

        return new VideosResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public void registerVideo(VideoUploadedEvent request){

        Video savedVideo = repository.save(Video.builder()
                .id(request.videoId())
                .title(request.title())
                .description(request.description())
                .creatorId(request.creatorId())
                .thumbnailUrl(request.thumbnailUrl())
                .originalVideoUrl(request.originalUrl())
                .status(VideoStatus.PENDING)
                .build());

        kafkaPublisher.sendVideoCatalogedEvent(new VideoCatalogedEvent(
                savedVideo.getId(),
                savedVideo.getOriginalVideoUrl()));

    }

    public void videoProcessSuccess(VideoProcessSuccessEvent event){
        Video video = repository.findById(event.videoId()).orElseThrow(()-> new VideoNotFoundException("Video not found"));

        video.setStatus(VideoStatus.READY);
        String duration = event.resolutions().remove("duration");
        video.setDuration(duration);
        video.setVideoUrls(event.resolutions());

        repository.save(video);

        kafkaPublisher.sendVideoNotification(new VideoNotificationEvent(
                video.getCreatorId(),
                video.getId(),
                "SUCCESS",
                "Your video is ready!"));
    }

    public void videoProcessFailed(VideoProcessFailureEvent event){
        Video video = repository.findById(event.videoId()).orElseThrow(()-> new VideoNotFoundException("Video not found"));

        video.setStatus(VideoStatus.FAILED);

        repository.save(video);

        kafkaPublisher.sendVideoNotification(new VideoNotificationEvent(
                video.getCreatorId(),
                video.getId(),
                "FAILED",
                "Your video could not be uploaded: "+event.errorMessage()));
    }
}
