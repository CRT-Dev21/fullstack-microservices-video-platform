package dev.crt.user.service.controller;

import dev.crt.user.service.dto.request.AvatarUpdateRequest;
import dev.crt.user.service.dto.request.LoginRequest;
import dev.crt.user.service.dto.request.RegisterRequest;
import dev.crt.user.service.dto.response.CreatorDetailsResponse;
import dev.crt.user.service.dto.response.UserResponse;
import dev.crt.user.service.service.UserService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest request) {
        userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        String token = userService.loginUser(request);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUserInfo(
            @RequestHeader("X-Creator-ID") UUID creatorId) {
        return ResponseEntity.ok(userService.getUserInfo(creatorId));
    }

    @GetMapping("/{creatorId}")
    public ResponseEntity<UserResponse> getCreatorInfo(
            @PathVariable UUID creatorId) {
        return ResponseEntity.ok(userService.getUserInfo(creatorId));
    }

    @GetMapping("/batch")
    public ResponseEntity<List<CreatorDetailsResponse>> getCreatorsDetails(
            @RequestParam List<UUID> ids) {

        if (ids.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<CreatorDetailsResponse> details = userService.getCreatorsDetails(ids);
        return ResponseEntity.ok(details);
    }

    @PutMapping("/avatars")
    public ResponseEntity<Map<String, String>> updateAvatarUrl(
            @RequestHeader("X-Creator-ID") UUID creatorId,
            @RequestBody AvatarUpdateRequest request) {

        userService.updateAvatar(creatorId, request.avatarUrl());

        return ResponseEntity.ok(Map.of("avatarUrl", request.avatarUrl()));
    }

    @GetMapping("/avatars")
    public ResponseEntity<Resource> serveAvatar(@RequestParam(required = false) String path) {
        Path rootPath = Paths.get("uploads");
        try {
            if (path == null || path.isBlank()) {
                ClassPathResource defaultResource = new ClassPathResource("static/avatars/default.png");
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(defaultResource);
            }

            String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
            decodedPath = decodedPath.replace("\\", "/");
            Path filePath = rootPath.resolve(decodedPath);

            Resource resource;
            if (Files.exists(filePath)) {
                resource = new FileSystemResource(filePath);
            } else {
                resource = new ClassPathResource("static/avatars/default.png");
            }

            MediaType mediaType = MediaType.IMAGE_PNG;
            String fileName = filePath.getFileName().toString().toLowerCase();
            if (fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) mediaType = MediaType.IMAGE_JPEG;

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);

        } catch (Exception e) {
            ClassPathResource defaultResource = new ClassPathResource("static/avatars/default.png");
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(defaultResource);
        }
    }
}