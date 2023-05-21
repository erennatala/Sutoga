package com.sutoga.backend.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentResponse {
    Long id;
    String text;
    Long userId;
    String profilePhotoUrl;
    String username;
}

