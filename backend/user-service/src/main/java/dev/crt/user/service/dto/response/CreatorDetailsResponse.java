package dev.crt.user.service.dto.response;

import java.util.UUID;

public record CreatorDetailsResponse(UUID creatorId, String username, String avatarUrl){
}
