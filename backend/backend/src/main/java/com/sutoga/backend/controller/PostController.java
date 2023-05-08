package com.sutoga.backend.controller;

import com.sutoga.backend.entity.Post;
import com.sutoga.backend.entity.dto.PostResponse;
import com.sutoga.backend.entity.request.CreatePostRequest;
import com.sutoga.backend.exceptions.ResultNotFoundException;
import com.sutoga.backend.service.PostService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public List<PostResponse> getAllPosts(){
        return postService.getAllPosts().stream().map(PostResponse::new).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<Void> createPost(@RequestBody CreatePostRequest newPost) {
        Post post = postService.createPost(newPost);
        if(post != null)
            return new ResponseEntity<>(HttpStatus.CREATED);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/{postId}")
    public PostResponse getOnePost(@PathVariable Long postId) {
        Post post = postService.getOnePostById(postId);
        if(post == null) {
            throw new ResultNotFoundException("Post with id "+ postId +" not found!" );
        }
        return new PostResponse(post);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Void> updateOnePost(@PathVariable Long postId, @RequestBody Post  newPost) {
        Post post = postService.updatePost(postId, newPost);
        if(post != null)
            return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping("/{postId}")
    public void deleteOnePost(@PathVariable Long postId) {
        postService.deleteById(postId);
    }

    @GetMapping("/media/{filename}")
    public ResponseEntity<InputStreamResource> serveMedia(@PathVariable("filename") String filename) {
        InputStream inputStream = postService.getMediaAsStream(filename);
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.builder("inline").filename(filename).build());

        return new ResponseEntity<>(inputStreamResource, headers, HttpStatus.OK);
    }


}
