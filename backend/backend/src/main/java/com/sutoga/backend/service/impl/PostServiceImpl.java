package com.sutoga.backend.service.impl;

import com.sutoga.backend.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.sutoga.backend.entity.Post;
import com.sutoga.backend.repository.PostRepository;
import com.sutoga.backend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Override
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @Override
    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    @Override
    public Post getOnePostById(Long postId) {
        Optional<Post> post = postRepository.findById(postId);
        return post.orElse(null);
    }

    @Override
    public Post updatePost(Long postId, Post newPost) {
        Optional<Post> existingPost = postRepository.findById(postId);
        if (existingPost.isPresent()) {
            Post post = existingPost.get();
            post.setDescription(newPost.getDescription());
            post.setUser(newPost.getUser());
            post.setComments(newPost.getComments());
            return postRepository.save(post);
        }
        return null;
    }

    @Override
    public void deleteById(Long postId) {
        postRepository.deleteById(postId);
    }
}
