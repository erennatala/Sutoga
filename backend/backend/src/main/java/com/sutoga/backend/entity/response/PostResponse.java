package com.sutoga.backend.entity.response;

import com.sutoga.backend.entity.Comment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostResponse {

    private Long id;
    private String description;
    private Long userId;
    private List<Comment> comments;
    private String mediaUrl;
    private LocalDateTime postDate;
    private String username;
    private Integer likeCount;
    private Integer commentCount;
    private boolean likedByUser;
    private boolean isUsersPost;
}