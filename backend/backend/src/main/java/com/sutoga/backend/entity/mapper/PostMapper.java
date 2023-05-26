package com.sutoga.backend.entity.mapper;

import com.sutoga.backend.entity.Post;
import com.sutoga.backend.entity.response.PostResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(target = "userId", expression = "java(mapUserToUserId(post))")
    @Mapping(target = "username", expression = "java(mapUserToUsername(post))")
    PostResponse postToPostResponse(Post post);

    default Long mapUserToUserId(Post post) {
        if (post != null && post.getUser() != null) {
            return post.getUser().getId();
        }
        return null;
    }

    default String mapUserToUsername(Post post) {
        if (post != null && post.getUser() != null) {
            return post.getUser().getUsername();
        }
        return null;
    }

    Post postResponseToPost(PostResponse postResponse);
}

