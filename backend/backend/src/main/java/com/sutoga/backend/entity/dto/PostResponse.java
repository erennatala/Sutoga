package com.sutoga.backend.entity.dto;

import com.sutoga.backend.entity.Comment;
import com.sutoga.backend.entity.Post;
import com.sutoga.backend.entity.User;
import lombok.Data;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.List;


@Data
public class PostResponse {
    private Long id;
    private String description;
    //private LocalDateTime createdAt;
    private UserResponse user;
    private List<CommentResponse> comments;

    public PostResponse(Post post) {
        this.id = post.getId();
        this.description = post.getDescription();
        //Post içine create date koymak lazım
        //this.createdAt = post.getCreatedAt();
        this.user = new UserResponse(post.getUser());
        // CommentResponse yazılınca düzelicek
        //this.comments = post.getComments().stream().map(CommentResponse::new).collect(Collectors.toList());
    }
}
