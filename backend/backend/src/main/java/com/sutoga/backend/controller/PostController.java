package com.sutoga.backend.controller;

import com.sutoga.backend.entity.Post;
import com.sutoga.backend.entity.request.CreatePostRequest;
import com.sutoga.backend.entity.response.PostResponse;
import com.sutoga.backend.exceptions.ResultNotFoundException;
import com.sutoga.backend.service.PostService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/posts")
@CrossOrigin(origins = "*")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/create")
    public ResponseEntity<PostResponse> createPost(@RequestParam("description") String description,
                                           @RequestParam("userId") Long userId,
                                           @RequestParam(value = "media", required = false) MultipartFile media) {
        try {
            CreatePostRequest newPost = new CreatePostRequest();
            newPost.setDescription(description);
            newPost.setUserId(userId);
            newPost.setMedia(media);

            PostResponse createdPost = postService.createPost(newPost);
            return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
        } catch (ResultNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Void> updateOnePost(@PathVariable Long postId, @RequestBody Post  newPost) {
        Post post = postService.updatePost(postId, newPost);
        if(post != null)
            return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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

    @GetMapping("/getHomePosts")
    public Page<PostResponse> getFriendsPosts(@RequestParam("userId") Long userId,
                                              @RequestParam(defaultValue = "0") int pageNumber,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        return postService.getMergedPosts(userId, pageNumber, pageSize);
    }

    @GetMapping("/getUserPosts")
    public Page<PostResponse> getUserPosts(@RequestParam("userId") Long userId,
                                      @RequestParam(defaultValue = "0") int pageNumber,
                                      @RequestParam(defaultValue = "10") int pageSize) {
        return postService.getProfilePosts(userId, pageNumber, pageSize);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteOnePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}