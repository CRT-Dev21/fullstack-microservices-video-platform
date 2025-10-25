package dev.crt.uploader.service.controller;

import dev.crt.uploader.service.service.UploadService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {
    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
            this.uploadService = uploadService;
        }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<?>> upload(
            @RequestPart("title") String title,
            @RequestPart("description") String description,
            @RequestHeader("X-Creator-ID") UUID creatorId,
            @RequestPart("image") FilePart image,
            @RequestPart("video") FilePart video) {

        return uploadService.uploadVideo(creatorId, title, description, image, video)
                .thenReturn(ResponseEntity.accepted()
                        .body(Map.of("message", "Video processing started.")));
    }

    @PostMapping(value = "/avatars", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Map<String, String>>> uploadAvatar(
            @RequestPart("avatar") FilePart avatar
    ){
        return uploadService.uploadAvatar(avatar)
                .map(avatarUrl -> ResponseEntity.ok(Map.of("avatarUrl", avatarUrl)));
    }
}