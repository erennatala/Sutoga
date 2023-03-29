package com.sutoga.backend.service;

import com.sutoga.backend.entity.Post;

import java.util.List;

public interface PostService {

    List<Post> getAllPosts();

    Post createPost(Post post);

    Post getOnePostById(Long postId);

    Post updatePost(Long postId, Post newPost);

    void deleteById(Long postId);
}
