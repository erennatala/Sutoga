package com.sutoga.backend.service.impl;

import com.sutoga.backend.entity.Comment;
import com.sutoga.backend.entity.Post;
import com.sutoga.backend.entity.request.CreateCommentRequest;
import com.sutoga.backend.entity.response.CommentResponse;
import com.sutoga.backend.exceptions.ResultNotFoundException;
import com.sutoga.backend.repository.CommentRepository;
import com.sutoga.backend.service.CommentService;
import com.sutoga.backend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserServiceImpl userServiceImpl;
    private final PostService postServiceImpl;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, UserServiceImpl userServiceImpl, PostService postServiceImpl) {
        this.commentRepository = commentRepository;
        this.userServiceImpl = userServiceImpl;
        this.postServiceImpl = postServiceImpl;
    }

    @Override
    public CommentResponse createComment(CreateCommentRequest createCommentRequest) {
        Post post = postServiceImpl.getPostById(createCommentRequest.getPostId());

        if(post != null){
            Comment comment = new Comment();
            comment.setPost(post);
            comment.setUser(userServiceImpl.getOneUserById(createCommentRequest.getSenderId()));
            comment.setText(createCommentRequest.getText());
            comment.setCommentDate(LocalDateTime.now());
            Comment savedComment = commentRepository.save(comment);

            return new CommentResponse(savedComment.getId(), savedComment.getText(), savedComment.getUser().getId(), savedComment.getUser().getProfilePhotoUrl(), savedComment.getUser().getUsername());
        }
        else{
            throw new ResultNotFoundException("Invalid postId");
        }
    }

    @Override
    public Page<CommentResponse> getCommentsByPostId(Long postId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findAllByPostId(postId, pageable);

        return comments.map(comment -> new CommentResponse(comment.getId(), comment.getText(), comment.getUser().getId(), comment.getUser().getProfilePhotoUrl(), comment.getUser().getUsername()));
    }

    @Override
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    @Override
    public Integer getCommentCountByPostId(Long postId) {
        return commentRepository.getCommentCountByPostId(postId);
    }
}

