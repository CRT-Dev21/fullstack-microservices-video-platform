package dev.crt.processor.service.kafka.events;

import java.util.UUID;

public record VideoCatalogedEvent(UUID videoId, String videoUrl){}
