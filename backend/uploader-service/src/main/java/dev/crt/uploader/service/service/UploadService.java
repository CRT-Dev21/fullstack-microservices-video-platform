package dev.crt.uploader.service.service;

import dev.crt.uploader.service.exception.UploadFailedException;
import dev.crt.uploader.service.kafka.events.VideoUploadFailedEvent;
import dev.crt.uploader.service.kafka.events.VideoUploadedEvent;
import dev.crt.uploader.service.kafka.producer.KafkaPublisher;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class UploadService {

    private final StorageService storageService;
    private final KafkaPublisher kafkaPublisher;

    public UploadService(StorageService storageService, KafkaPublisher kafkaPublisher) {
        this.storageService = storageService;
        this.kafkaPublisher = kafkaPublisher;
    }

    public Mono<Void> uploadVideo(UUID creatorId, String title, String description,
                                  FilePart image, FilePart video) {

        return Mono.zip(
                        storageService.uploadImage(image),
                        storageService.uploadVideo(video)
                )
                .flatMap(tuple -> {
                    StorageService.UploadImageResult img = tuple.getT1();
                    StorageService.UploadVideoResult vid = tuple.getT2();

                    VideoUploadedEvent successEvent = new VideoUploadedEvent(
                            vid.getVideoId(),
                            creatorId,
                            title,
                            description,
                            img.getPublicUrl(),
                            vid.getPublicUrl()
                    );

                    return kafkaPublisher.sendEvent("video.uploaded.event", successEvent.videoId().toString(), successEvent)
                            .onErrorResume(err -> Mono.when(
                                    storageService.delete(img.getImageName()).onErrorResume(e -> Mono.empty()),
                                    storageService.delete(vid.getVideoId().toString()).onErrorResume(e -> Mono.empty())
                            ).then(Mono.error(err)));
                })
                .onErrorResume(error -> {
                    VideoUploadFailedEvent failEvent = new VideoUploadFailedEvent(
                            creatorId,
                            "FAILED",
                            "An error occurred while uploading your video. Please try again later."
                    );
                    return kafkaPublisher.sendEvent("video.upload.failed.event", creatorId.toString(), failEvent)
                            .then(Mono.error(new UploadFailedException("Upload/Processing pipeline failed.", error)));
                });
    }

    public Mono<String> uploadAvatar(FilePart avatar){
        return storageService.uploadAvatar(avatar);
    }
}

