package com.sutoga.backend.controller;

import com.sutoga.backend.entity.Comment;
import com.sutoga.backend.entity.request.CreateCommentRequest;
import com.sutoga.backend.entity.response.CommentResponse;
import com.sutoga.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
@CrossOrigin(origins = "*")
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public List<CommentResponse> getAllCommentsByParameter(
            @RequestParam Optional<Long> postId) {
        return commentService.getAllCommentsByParameter(postId);
    }

    @PostMapping
    public Comment createComment(@RequestBody CreateCommentRequest createCommentRequest) {
        return commentService.createComment(createCommentRequest);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@RequestParam Long commentId) {
        commentService.deleteComment(commentId);
    }


}
