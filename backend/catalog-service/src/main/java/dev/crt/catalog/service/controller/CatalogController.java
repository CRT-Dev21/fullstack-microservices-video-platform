package dev.crt.catalog.service.controller;

import dev.crt.catalog.service.dto.VideosResponse;
import dev.crt.catalog.service.exception.ResourceNotFoundException;
import dev.crt.catalog.service.service.CatalogService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {
    private final CatalogService service;

    public CatalogController(CatalogService service){
            this.service = service;
        }

    @GetMapping("/videos")
    public ResponseEntity<VideosResponse> getVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query)
    {
        VideosResponse response = service.getVideosOrSearch(query, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/related")
    public ResponseEntity<VideosResponse> getRelatedVideos(
            @RequestParam String title,
            @RequestParam UUID currentVideoId,
            @RequestParam(defaultValue = "10") int limit)
    {
        VideosResponse relatedVideos = service.getRelatedVideos(title, currentVideoId, limit);
        return ResponseEntity.ok(relatedVideos);
    }

    @GetMapping("/videos/me")
    public ResponseEntity<VideosResponse> getMyVideosByCreatorId(
            @RequestHeader("X-Creator-ID") UUID creatorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){

        VideosResponse response = service.getVideosByCreatorIdForOwner(creatorId, page, size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/videos/{creatorId}")
    public ResponseEntity<VideosResponse> getVideosByCreatorId(
            @PathVariable UUID creatorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){

        VideosResponse response = service.getVideosByCreatorIdForPublic(creatorId, page, size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/images")
    public ResponseEntity<Resource> serveVideoImage(@RequestParam String path) {
        try {
            String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8)
                    .replace("\\", "/");

            Path basePath = Paths.get("uploads");
            Path filePath = basePath.resolve(decodedPath).normalize();

            if (!Files.exists(filePath)) {
                throw new ResourceNotFoundException("Image not found at path: "+path);
            }

            Resource resource = new FileSystemResource(filePath);
            MediaType mediaType = getMediaType(filePath);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private MediaType getMediaType(Path filePath) {
        String name = filePath.getFileName().toString().toLowerCase();
        if (name.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (name.endsWith(".webp")) return MediaType.valueOf("image/webp");
        return MediaType.IMAGE_JPEG;
    }

}
