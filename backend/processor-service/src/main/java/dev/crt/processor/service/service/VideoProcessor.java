package dev.crt.processor.service.service;

import com.github.kokorin.jaffree.LogLevel;
import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import dev.crt.processor.service.kafka.events.*;
import dev.crt.processor.service.kafka.producer.KafkaPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class VideoProcessor {

    private final KafkaPublisher kafkaPublisher;
    private final Path rootPath = Paths.get("uploads");

    public VideoProcessor(KafkaPublisher kafkaPublisher) {
        this.kafkaPublisher = kafkaPublisher;
    }

    public Mono<Map<String, String>> processVideo(UUID videoId, String videoPath) {
        Path input = rootPath.resolve(videoPath);
        Path outputFolder = input.getParent();
        String baseName = input.getFileName().toString();
        int dot = baseName.lastIndexOf('.');
        String nameNoExt = dot > 0 ? baseName.substring(0, dot) : baseName;

        if (!Files.exists(input)) {
            return Mono.error(new IllegalArgumentException("Video file not found: " + videoPath));
        }

        Map<String, String> resolutions = Map.of(
                "1080p", "1920:1080",
                "720p",  "1280:720",
                "360p",  "640:360"
        );

        Mono<String> durationMono = Mono.fromCallable(() -> {
            FFprobeResult probeResult = FFprobe.atPath()
                    .setInput(input)
                    .addArgument("-show_format")
                    .addArguments("-v", "error")
                    .execute();

            com.github.kokorin.jaffree.ffprobe.Format format = probeResult.getFormat();

            if (format == null || format.getDuration() == null) {
                throw new IllegalStateException("FFprobe failed to parse duration for: " + input.getFileName());
            }

            Float duration = format.getDuration();
            long totalSeconds = duration.longValue();

            return formatDuration(totalSeconds);
        }).subscribeOn(Schedulers.boundedElastic());

        Mono<Map<String, String>> transcodingMono = Flux.fromIterable(resolutions.entrySet())
                .flatMap(entry ->
                        Mono.fromCallable(() -> {
                            String quality = entry.getKey();
                            String size = entry.getValue();

                            Path qualityFolder = outputFolder.resolve(nameNoExt + "_" + quality);
                            Files.createDirectories(qualityFolder);

                            Path transcodedPath = qualityFolder.resolve(nameNoExt + "_" + quality + ".mp4");
                            Files.deleteIfExists(transcodedPath);

                            FFmpeg.atPath()
                                    .addInput(UrlInput.fromPath(input))
                                    .addOutput(UrlOutput.toPath(transcodedPath))
                                    .setFilter(StreamType.VIDEO, "scale=" + size)
                                    .addArguments("-c:v", "libx264")
                                    .addArguments("-preset", "fast")
                                    .addArguments("-crf", "23")
                                    .addArguments("-c:a", "aac")
                                    .addArguments("-b:a", "128k")
                                    .setLogLevel(LogLevel.INFO)
                                    .execute();

                            Path manifest = qualityFolder.resolve("index.m3u8");
                            Files.deleteIfExists(manifest);

                            FFmpeg.atPath()
                                    .addInput(UrlInput.fromPath(transcodedPath))
                                    .addOutput(UrlOutput.toPath(manifest))
                                    .addArguments("-c:v", "copy")
                                    .addArguments("-c:a", "copy")
                                    .addArguments("-hls_time", "8")
                                    .addArguments("-hls_list_size", "0")
                                    .addArguments("-f", "hls")
                                    .setLogLevel(LogLevel.INFO)
                                    .execute();

                            String relativePath = rootPath.relativize(manifest).toString().replace("\\", "/");
                            return Map.entry(quality, relativePath);
                        }).subscribeOn(Schedulers.boundedElastic())
                )
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);

        return Mono.zip(transcodingMono, durationMono)
                .map(tuple -> {
                    Map<String, String> urlsMap = tuple.getT1();
                    String formattedDuration = tuple.getT2();
                    urlsMap.put("duration", formattedDuration);

                    return urlsMap;
                });
    }

    private String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            // HH:MM:SS
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            // MM:SS
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    public Mono<Void> publishProcessingSuccess(UUID videoId, Map<String, String> videoUrls) {
        VideoProcessSuccessEvent successEvent = new VideoProcessSuccessEvent(
                videoId,
                "SUCCESS",
                videoUrls
        );
        return kafkaPublisher.sendEvent("video.process.result", videoId.toString(), successEvent);
    }

    public Mono<Void> publishProcessingFailure(UUID videoId, String errorCode, String errorMessage) {
        VideoProcessFailureEvent failureEvent = new VideoProcessFailureEvent(
                videoId,
                "FAILURE",
                errorCode,
                errorMessage
        );
        return kafkaPublisher.sendEvent("video.process.result", videoId.toString(), failureEvent);
    }
}
