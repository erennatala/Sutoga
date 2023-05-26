package com.sutoga.backend.service;

import com.sutoga.backend.entity.Like;
import com.sutoga.backend.entity.request.CreateLikeRequest;
import com.sutoga.backend.entity.response.FriendResponse;
import com.sutoga.backend.entity.response.LikeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LikeService {
    Like createLike(CreateLikeRequest createLikeRequest);

    List<LikeResponse> getAllLikesByParameter(Optional<Long> userId, Optional<Long> postId);

    void deleteLike(Long id);
    void deleteLikeByPostIdAndUserId(Long postId, Long userId);
    void deleteLikesByPostId(Long postId);
    boolean isPostLikedByUser(Long postId, Long userId);
    List<FriendResponse> getLikersByPostId(Long postId, Long appUserId);
    Page<Like> getUserLikedPosts(Long userId, Pageable pageable);
}
