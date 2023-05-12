package com.sutoga.backend.service;

import com.sutoga.backend.entity.Post;
import com.sutoga.backend.entity.request.CreatePostRequest;
import org.springframework.data.domain.Page;

import java.io.InputStream;
import java.util.List;

public interface PostService {

    List<Post> getAllPosts();

    Post createPost(CreatePostRequest newPost);

    Post getOnePostById(Long postId);

    Post updatePost(Long postId, Post newPost);

    void deleteById(Long postId);

    Page<Post> getUserPosts(Long userId, int pageNumber, int pageSize);

    Page<Post> getFriendsPosts(Long userId, int pageNumber, int pageSize);
    InputStream getMediaAsStream(String objectName);
}
