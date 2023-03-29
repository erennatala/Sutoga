package com.sutoga.backend.service;

import com.sutoga.backend.entity.Comment;
import com.sutoga.backend.entity.request.CreateCommentRequest;
import com.sutoga.backend.entity.response.CommentResponse;

import java.util.List;
import java.util.Optional;

public interface CommentService {

    Comment createComment(CreateCommentRequest createCommentRequest);

    List<CommentResponse> getAllCommentsByParameter(Optional<Long> postId);

    void deleteComment(Long id);
}
