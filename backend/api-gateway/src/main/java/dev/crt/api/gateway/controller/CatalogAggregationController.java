package dev.crt.api.gateway.controller;

import dev.crt.api.gateway.dto.*;
import dev.crt.api.gateway.exception.ServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogAggregationController {
    private final WebClient catalogServiceWebClient;
    private final WebClient userServiceWebClient;

    public CatalogAggregationController(WebClient catalogServiceWebClient, WebClient userServiceWebClient){
        this.catalogServiceWebClient = catalogServiceWebClient;
        this.userServiceWebClient = userServiceWebClient;
    }

    @GetMapping("/enriched-videos")
    public Mono<ResponseEntity<EnrichedVideosResponse>> getEnrichedVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query){

        Mono<VideosResponse> videosResponseMono = catalogServiceWebClient.get()
                .uri(uriBuilder ->
                    uriBuilder.path("/videos")
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .queryParamIfPresent("query", (query != null && !query.isBlank()) ? Optional.of(query) : Optional.empty())
                            .build()
                )
                .retrieve()
                .bodyToMono(VideosResponse.class)
                .onErrorResume(e -> {
                    System.err.println("Error calling catalog service: "+e.getMessage());
                    return Mono.error(() -> new ServiceUnavailableException("Catalog Service", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
                });

        return videosResponseMono.flatMap(videosResponse -> {

            if(videosResponse.content().isEmpty()){
                return Mono.just(ResponseEntity.ok(new EnrichedVideosResponse(
                        List.of(), videosResponse.page(), videosResponse.size(), 0, 0
                )));
            }

            Set<UUID> creatorIds = videosResponse.content().stream()
                    .map(VideoMetadata::creatorId)
                    .collect(Collectors.toSet());

            String ids = creatorIds.stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining(","));

            Mono<Map<UUID, CreatorDetails>> usersMapMono = userServiceWebClient.get()
                    .uri(uriBuilder ->
                            uriBuilder.path("/batch")
                                    .queryParam("ids", ids)
                                    .build()
                    )
                    .retrieve()
                    .bodyToFlux(CreatorDetails.class)
                    .collectList()
                    .map(userList ->
                        userList.stream().collect(Collectors.toMap(CreatorDetails::creatorId, Function.identity())))
                    .onErrorResume( e -> {
                                System.err.println("Error calling user service: ");
                                return Mono.error(() -> new ServiceUnavailableException("User Service", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
                    });

            return usersMapMono.map(usersMap -> {
                List<EnrichedVideoMetadata> enrichedContent = videosResponse.content().stream().map(video -> {
                    CreatorDetails creatorDetails = usersMap.get(video.creatorId());

                    return new EnrichedVideoMetadata(
                            video.videoId(),
                            video.title(),
                            video.description(),
                            video.thumbnailUrl(),
                            video.videoUrls(),
                            video.duration(),
                            video.status(),
                            creatorDetails != null ? creatorDetails : new CreatorDetails(video.creatorId(), "NOT FOUND", "")
                    );
                }).toList();

                EnrichedVideosResponse enrichedVideosResponse = new EnrichedVideosResponse(
                        enrichedContent,
                        videosResponse.page(),
                        videosResponse.size(),
                        videosResponse.totalElements(),
                        videosResponse.totalPages());

                return ResponseEntity.ok(enrichedVideosResponse);
            });
        });

    }

    @GetMapping("/enriched-related-videos")
    public Mono<ResponseEntity<EnrichedVideosResponse>> getEnrichedRelatedVideos(
            @RequestParam String title,
            @RequestParam UUID currentVideoId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Mono<VideosResponse> videosResponseMono = catalogServiceWebClient.get()
                .uri(uriBuilder ->
                        uriBuilder.path("/related")
                                .queryParam("title",title)
                                .queryParam("currentVideoId", currentVideoId)
                                .queryParam("limit", limit)
                                .build()
                )
                .retrieve()
                .bodyToMono(VideosResponse.class)
                .onErrorResume( e -> {
                    System.err.println("Error calling catalog service: "+e.getMessage());
                    return Mono.error(() -> new ServiceUnavailableException("Catalog Service", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
                });

        return videosResponseMono.flatMap(videosResponse -> {

            if(videosResponse.content().isEmpty()){
                return Mono.just(ResponseEntity.ok(new EnrichedVideosResponse(
                        List.of(), videosResponse.page(), videosResponse.size(), 0, 0
                )));
            }

            Set<UUID> creatorIds = videosResponse.content().stream()
                    .map(VideoMetadata::creatorId)
                    .collect(Collectors.toSet());

            String ids = creatorIds.stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining(","));

            Mono<Map<UUID, CreatorDetails>> usersMapMono = userServiceWebClient.get()
                    .uri(uriBuilder ->
                            uriBuilder.path("/batch")
                                    .queryParam("ids", ids)
                                    .build()
                    )
                    .retrieve()
                    .bodyToFlux(CreatorDetails.class)
                    .collectList()
                    .map(userList ->
                            userList.stream().collect(Collectors.toMap(CreatorDetails::creatorId, Function.identity())))
                    .onErrorResume( e -> {
                        System.err.println("Error calling user service: ");
                        return Mono.error(() -> new ServiceUnavailableException("User Service", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
                    });

            return usersMapMono.map(usersMap -> {
                List<EnrichedVideoMetadata> enrichedContent = videosResponse.content().stream().map(video -> {
                    CreatorDetails creatorDetails = usersMap.get(video.creatorId());

                    return new EnrichedVideoMetadata(
                            video.videoId(),
                            video.title(),
                            video.description(),
                            video.thumbnailUrl(),
                            video.videoUrls(),
                            video.duration(),
                            video.status(),
                            creatorDetails != null ? creatorDetails : new CreatorDetails(video.creatorId(), "NOT FOUND", "")
                    );
                }).toList();

                EnrichedVideosResponse enrichedVideosResponse = new EnrichedVideosResponse(
                        enrichedContent,
                        videosResponse.page(),
                        videosResponse.size(),
                        videosResponse.totalElements(),
                        videosResponse.totalPages());

                return ResponseEntity.ok(enrichedVideosResponse);
            });
        });

    }
}
