package dev.crt.uploader.service.service.impl;

import dev.crt.uploader.service.exception.StorageException;
import dev.crt.uploader.service.service.StorageService;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private final Path rootPath = Paths.get("uploads");

    public LocalStorageService() throws IOException {
        if (!Files.exists(rootPath)) {
            Files.createDirectories(rootPath.resolve("images"));
            Files.createDirectories(rootPath.resolve("videos"));
            Files.createDirectories(rootPath.resolve("avatars"));
        }
    }

    @Override
    public Mono<UploadImageResult> uploadImage(FilePart image) {
        Path folderPath = rootPath.resolve("images");
        String imageName = System.currentTimeMillis() + "_" + image.filename();
        Path dest = folderPath.resolve(imageName);

        return Mono.fromCallable(() -> {
                    Files.createDirectories(folderPath);
                    return dest;
                })
                .flatMap(image::transferTo)
                .then(Mono.fromSupplier(() -> {
                    String relativePath = rootPath.relativize(dest).toString().replace("\\", "/");
                    return new UploadImageResult(imageName, relativePath);
                }));
    }

    @Override
    public Mono<UploadVideoResult> uploadVideo(FilePart filePart) {
        Path folderPath = rootPath.resolve("videos");
        UUID videoId = UUID.randomUUID();
        Path videoFolder = folderPath.resolve(videoId.toString());
        Path dest = videoFolder.resolve("original.mp4");

        return Mono.fromCallable(() -> {
                    Files.createDirectories(videoFolder);
                    return dest;
                })
                .flatMap(filePart::transferTo)
                .then(Mono.fromSupplier(() -> {
                    String relativePath = rootPath.relativize(dest).toString().replace("\\", "/");
                    return new UploadVideoResult(videoId, relativePath);
                }));
    }

    @Override
    public Mono<String> uploadAvatar(FilePart avatar) {
        String fileName = UUID.randomUUID() + "_" + avatar.filename();
        Path folderPath = rootPath.resolve("avatars");
        Path dest = folderPath.resolve(fileName);

        return Mono.fromCallable(() -> {
                    Files.createDirectories(folderPath);
                    return dest;
                })
                .flatMap(avatar::transferTo)
                .thenReturn("avatars/" + fileName);
    }

    @Override
    public Mono<Void> delete(String objectName) {
        try {
            Path filePath = rootPath.resolve(objectName).normalize();
            Files.deleteIfExists(filePath);
            return Mono.empty();
        } catch (IOException e) {
            return Mono.error(new StorageException("Failed to delete file: "+objectName, e));
        }
    }
}
