package com.sutoga.backend.service.impl;

import com.sutoga.backend.entity.Comment;
import com.sutoga.backend.entity.Notification;
import com.sutoga.backend.entity.Post;
import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.request.CreateCommentRequest;
import com.sutoga.backend.entity.response.CommentResponse;
import com.sutoga.backend.exceptions.ResultNotFoundException;
import com.sutoga.backend.repository.CommentRepository;
import com.sutoga.backend.repository.NotificationRepository;
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
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, UserServiceImpl userServiceImpl, PostService postServiceImpl, NotificationService notificationService, NotificationRepository notificationRepository) {
        this.commentRepository = commentRepository;
        this.userServiceImpl = userServiceImpl;
        this.postServiceImpl = postServiceImpl;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public CommentResponse createComment(CreateCommentRequest createCommentRequest) {
        User user = userServiceImpl.getOneUserById(createCommentRequest.getSenderId());
        Post post = postServiceImpl.getPostById(createCommentRequest.getPostId());

        if(post != null){
            Comment comment = new Comment();
            comment.setPost(post);
            comment.setUser(userServiceImpl.getOneUserById(createCommentRequest.getSenderId()));
            comment.setText(createCommentRequest.getText());
            comment.setCommentDate(LocalDateTime.now());
            Comment savedComment = commentRepository.save(comment);

            Notification notification = new Notification();
            notification.setReceiver(comment.getPost().getUser());
            notification.setCommentActivity(comment);
            notification.setSeen(false);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setSenderUsername(user.getUsername());
            notification.setSenderPhotoUrl(user.getProfilePhotoUrl());

            // Save and send notification
            notificationService.createAndSendNotification(notification);

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
        Comment comment = commentRepository.findById(id).orElse(null);
        Notification notification = notificationRepository.findByCommentActivity(comment);
        if(notification != null) {
            notification.setCommentActivity(null);
            notificationRepository.save(notification);
        }
        commentRepository.deleteById(id);
        if (notification != null) {
            notificationRepository.delete(notification);
        }
    }

    @Override
    public Integer getCommentCountByPostId(Long postId) {
        return commentRepository.getCommentCountByPostId(postId);
    }
}

