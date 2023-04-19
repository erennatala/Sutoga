package com.sutoga.backend.service;

import com.sutoga.backend.entity.Post;
import com.sutoga.backend.entity.request.CreatePostRequest;

import java.util.List;

public interface PostService {

    List<Post> getAllPosts();

    Post createPost(CreatePostRequest post);

    Post getOnePostById(Long postId);

    Post updatePost(Long postId, Post newPost);

    void deleteById(Long postId);

    List<Post> getUserPosts();

    List<Post> getFriendsPosts();
}
