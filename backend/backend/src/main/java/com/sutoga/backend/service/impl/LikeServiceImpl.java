package com.sutoga.backend.service.impl;

import com.sutoga.backend.entity.Like;
import com.sutoga.backend.entity.Post;
import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.request.CreateLikeRequest;
import com.sutoga.backend.entity.response.FriendResponse;
import com.sutoga.backend.entity.response.LikeResponse;
import com.sutoga.backend.exceptions.ResultNotFoundException;
import com.sutoga.backend.repository.LikeRepository;
import com.sutoga.backend.service.LikeService;
import com.sutoga.backend.service.PostService;
import com.sutoga.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final UserService userServiceImpl;

    @Lazy
    private final PostService postServiceImpl;

    @Autowired
    public LikeServiceImpl(LikeRepository likeRepository, UserService userService, @Lazy PostService postService) {
        this.likeRepository = likeRepository;
        this.userServiceImpl = userService;
        this.postServiceImpl = postService;
    }
    @Override
    public Like createLike(CreateLikeRequest createLikeRequest) {
        User user = userServiceImpl.getOneUserById(createLikeRequest.getUserId());
        Post post = postServiceImpl.getPostById(createLikeRequest.getPostId());

        if (user != null && post != null) {
            Like existingLike = likeRepository.findByPostIdAndUserId(createLikeRequest.getPostId(), createLikeRequest.getUserId());
            if (existingLike != null) {
                // A like already exists for this user and post.
                // You can throw an exception or return the existingLike directly, depending on your needs.
                throw new IllegalArgumentException("User has already liked this post");
            }

            Like like = new Like();
            like.setUser(user);
            like.setPost(post);

            return likeRepository.save(like);
        } else {
            throw new ResultNotFoundException("Invalid userId or postId");
        }
    }

    @Override
    public List<LikeResponse> getAllLikesByParameter(Optional<Long> userId, Optional<Long> postId) {
        List<Like> likeList;
        if(userId.isPresent() && postId.isPresent()){
            likeList=likeRepository.findByUserIdAndPostId(userId.get(),postId.get());
        } else if (userId.isPresent()) {
            likeList = likeRepository.findByUserId(userId.get());
        } else if (postId.isPresent()) {
            likeList = likeRepository.findByPostId(postId.get());
        }
        else {
            likeList= likeRepository.findAll();
        }
        return likeList.stream().map(like ->
            new LikeResponse(like.getId(), like.getUser().getId(), like.getPost().getId())).collect(Collectors.toList());
    }

    @Override
    public void deleteLike(Long id) {
        likeRepository.deleteById(id);
    }

    @Override
    public void deleteLikesByPostId(Long postId) {
        List<Like> likes = likeRepository.findByPostId(postId);
        likeRepository.deleteAll(likes);
    }

    @Override
    public void deleteLikeByPostIdAndUserId(Long postId, Long userId) {
        Like like = likeRepository.findByPostIdAndUserId(postId, userId);
        if (like != null) {
            likeRepository.delete(like);
        } else {
            throw new IllegalArgumentException("Like not found for provided postId and userId");
        }
    }

    @Override
    public boolean isPostLikedByUser(Long postId, Long userId) {
        Like like = likeRepository.findByPostIdAndUserId(postId, userId);
        return like != null;
    }

    @Override
    public List<FriendResponse> getLikersByPostId(Long postId, Long appUserId) {
        Post post = postServiceImpl.getPostById(postId);

        List<Like> likes = post.getLikes();
        List<FriendResponse> likers = new ArrayList<>();

        for (Like like : likes) {
            FriendResponse liker = new FriendResponse();
            liker.setId(like.getUser().getId());
            liker.setUsername(like.getUser().getUsername());
            liker.setProfilePhotoUrl(like.getUser().getProfilePhotoUrl());
            liker.setIsFriend(userServiceImpl.areFriends(like.getUser().getId(), appUserId));

            likers.add(liker);
        }

        return likers;
    }

}
