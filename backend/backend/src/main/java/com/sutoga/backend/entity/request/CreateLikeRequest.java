package com.sutoga.backend.entity.request;

import lombok.Data;

@Data
public class CreateLikeRequest {
    Long userId;
    Long postId;
}
