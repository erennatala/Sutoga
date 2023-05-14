package com.sutoga.backend.service.impl;

import com.sutoga.backend.entity.Comment;
import com.sutoga.backend.entity.Post;
import com.sutoga.backend.entity.request.CreateCommentRequest;
import com.sutoga.backend.entity.response.CommentResponse;
import com.sutoga.backend.exceptions.ResultNotFoundException;
import com.sutoga.backend.repository.CommentRepository;
import com.sutoga.backend.service.CommentService;
import com.sutoga.backend.service.LikeService;
import com.sutoga.backend.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserServiceImpl userServiceImpl;

    @Lazy
    private final PostService postServiceImpl;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, UserServiceImpl userServiceImpl, @Lazy PostService postServiceImpl) {
        this.commentRepository = commentRepository;
        this.userServiceImpl = userServiceImpl;
        this.postServiceImpl = postServiceImpl;
    }

    @Override
    public Comment createComment(CreateCommentRequest createCommentRequest) {
        //Post post = postServiceImpl.getOnePostById(createCommentRequest.getPostId());

        if(createCommentRequest!=null){
            Comment comment = new Comment();
            //comment.setPost(post);
            comment.setText(createCommentRequest.getText());

            return commentRepository.save(comment);
        }
        else{
            throw new ResultNotFoundException("Invalid postId");
        }
    }

    @Override
    public List<CommentResponse> getAllCommentsByParameter(Optional<Long> postId) {
        List<Comment> commentList;
        if(postId.isPresent()){
            commentList=commentRepository.findByPostId(postId.get());
        }
        else {
            commentList= commentRepository.findAll();
        }
        return commentList.stream().map(comment ->
                new CommentResponse(comment.getId(), comment.getPost().getId(), comment.getText())).collect(Collectors.toList());


    }

    @Override
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }


}
