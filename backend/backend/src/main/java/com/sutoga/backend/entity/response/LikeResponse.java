package com.sutoga.backend.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeResponse {

    Long id;

    Long userId;

    Long postId;

}
