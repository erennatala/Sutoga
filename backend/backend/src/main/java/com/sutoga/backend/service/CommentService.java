package com.sutoga.backend.service;

import com.sutoga.backend.entity.request.CreateCommentRequest;
import com.sutoga.backend.entity.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    CommentResponse createComment(CreateCommentRequest createCommentRequest);
    Page<CommentResponse> getCommentsByPostId(Long postId, Pageable pageable);
    void deleteComment(Long id);
    Integer getCommentCountByPostId(Long postId);
}
