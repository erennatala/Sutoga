package com.sutoga.backend.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentResponse {

    Long id;

    Long postId;

    String text;

}
