package dev.crt.api.gateway.dto;

import java.util.UUID;

public record CreatorDetails(UUID creatorId, String username, String avatarUrl){}
