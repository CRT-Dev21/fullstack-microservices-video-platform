package dev.crt.uploader.service.service;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface StorageService {

    Mono<UploadImageResult> uploadImage(FilePart image);
    Mono<UploadVideoResult> uploadVideo(FilePart video);
    Mono<String> uploadAvatar(FilePart avatar);
    Mono<Void> delete(String objectName);

    class UploadImageResult {
        private final String imageName;
        private final String publicUrl;

        public UploadImageResult(String imageName, String publicUrl){
            this.imageName = imageName;
            this.publicUrl = publicUrl;
        }
        public String getImageName() { return imageName; }
        public String getPublicUrl() { return publicUrl; }
    }

    class UploadVideoResult {
        private final UUID videoId;
        private final String publicUrl;

        public UploadVideoResult(UUID videoId, String publicUrl){
            this.videoId = videoId;
            this.publicUrl = publicUrl;
        }
        public UUID getVideoId() { return videoId; }
        public String getPublicUrl() { return publicUrl; }
    }
}
