package dev.crt.stream.service.controller;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/v1/stream")
public class VideoStreamController {

    private final DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();
    private static final int BUFFER_SIZE = 256 * 1024;
    private static final Path rootPath = Paths.get("uploads");

    @GetMapping
    public Mono<ResponseEntity<Flux<DataBuffer>>> streamVideo(
            @RequestParam String path,
            @RequestHeader HttpHeaders headers) throws IOException {

        String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
        Path filePath = rootPath.resolve(decodedPath);

        if (!Files.exists(filePath)) {
            return Mono.just(ResponseEntity.notFound().build());
        }

        if (decodedPath.toLowerCase().endsWith(".m3u8")) {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            String parentDir = rootPath.relativize(filePath.getParent()).toString().replace("\\", "/");

            content = content.replaceAll("(?m)^(.+\\.ts)$",
                    "http://localhost:8080/api/v1/stream?path=" + parentDir + "/$1");

            DataBuffer buffer = bufferFactory.wrap(content.getBytes(StandardCharsets.UTF_8));
            return Mono.just(ResponseEntity.ok()
                    .contentType(MediaType.valueOf("application/x-mpegurl"))
                    .body(Flux.just(buffer)));
        }

        long fileSize = Files.size(filePath);
        List<HttpRange> ranges = headers.getRange();
        MediaType mediaType = determineMediaType(decodedPath);

        if (ranges.isEmpty()) {
            Flux<DataBuffer> body = DataBufferUtils.read(
                    filePath, bufferFactory, BUFFER_SIZE, StandardOpenOption.READ
            );
            return Mono.just(ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(fileSize)
                    .body(body));
        }

        HttpRange range = ranges.get(0);
        long start = range.getRangeStart(fileSize);
        long end = range.getRangeEnd(fileSize);
        AtomicLong position = new AtomicLong(start);

        Flux<DataBuffer> body = DataBufferUtils.readAsynchronousFileChannel(
                        () -> AsynchronousFileChannel.open(filePath, StandardOpenOption.READ),
                        start, bufferFactory, BUFFER_SIZE)
                .handle((dataBuffer, sink) -> {
                    long bufferStart = position.get();
                    long bufferEnd = bufferStart + dataBuffer.readableByteCount() - 1;

                    if (bufferEnd > end) {
                        int sliceSize = (int) (end - bufferStart + 1);

                        DataBuffer slice = dataBuffer.split(sliceSize);
                        DataBufferUtils.release(dataBuffer);
                        sink.next(slice);

                        position.addAndGet(sliceSize);
                    } else {
                        sink.next(dataBuffer);
                        position.addAndGet(dataBuffer.readableByteCount());
                    }

                    if (position.get() > end) {
                        sink.complete();
                    }
                });

        return Mono.just(ResponseEntity.status(206)
                .contentType(mediaType)
                .header("Content-Range", "bytes " + start + "-" + end + "/" + fileSize)
                .contentLength(end - start + 1)
                .body(body));
    }

    private MediaType determineMediaType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".m3u8")) return MediaType.valueOf("application/x-mpegurl");
        if (lower.endsWith(".ts")) return MediaType.valueOf("video/mp2t");
        if (lower.endsWith(".mp4")) return MediaType.valueOf("video/mp4");
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}

