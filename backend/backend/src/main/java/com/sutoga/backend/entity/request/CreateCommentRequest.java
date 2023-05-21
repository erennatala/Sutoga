package com.sutoga.backend.entity.request;

import lombok.Data;

@Data
public class CreateCommentRequest {
    Long postId;
    String text;
    Long senderId;
}
