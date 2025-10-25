package dev.crt.user.service.dto;

import dev.crt.user.service.domain.User;
import dev.crt.user.service.dto.response.CreatorDetailsResponse;

public class UserMapper {

    public static CreatorDetailsResponse toCreatorDetails(User user) {

        if(user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()){
            user.setAvatarUrl("static/avatars/default.png");
        }

        return new CreatorDetailsResponse(user.getId(), user.getUsername(), normalizePath(user.getAvatarUrl()));
    }

    private static String normalizePath(String path){
        if(path == null) return null;

        return path.replace("\\", "/")
                .replace(" ", "%20");
    }
}
