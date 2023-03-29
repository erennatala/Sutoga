package com.sutoga.backend.controller;

import com.sutoga.backend.entity.Comment;
import com.sutoga.backend.entity.request.CreateCommentRequest;
import com.sutoga.backend.entity.response.CommentResponse;
import com.sutoga.backend.service.impl.CommentServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/comments")
@CrossOrigin(origins = "*")
public class CommentController {

    private CommentServiceImpl commentServiceImpl;

    public CommentController(CommentServiceImpl commentServiceImpl) {
        this.commentServiceImpl = commentServiceImpl;
    }

    @GetMapping
    public List<CommentResponse> getAllCommentsByParameter(
            @RequestParam Optional<Long> postId) {
        return commentServiceImpl.getAllCommentsByParameter(postId);
    }

    @PostMapping
    public Comment createComment(@RequestBody CreateCommentRequest createCommentRequest) {
        return commentServiceImpl.createComment(createCommentRequest);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@RequestParam Long commentId) {
        commentServiceImpl.deleteComment(commentId);
    }


}
