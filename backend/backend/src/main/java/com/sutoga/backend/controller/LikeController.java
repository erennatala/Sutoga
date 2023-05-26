package com.sutoga.backend.controller;

import com.sutoga.backend.entity.Like;
import com.sutoga.backend.entity.request.CreateLikeRequest;
import com.sutoga.backend.entity.response.FriendResponse;
import com.sutoga.backend.entity.response.LikeResponse;
import com.sutoga.backend.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/likes")
@CrossOrigin(origins = "*")
public class LikeController {

    private final LikeService likeServiceImpl;

    @GetMapping
    public ResponseEntity<List<LikeResponse>> getAllLikesByParameter(@RequestParam Optional<Long> userId,
                                                     @RequestParam Optional<Long> postId) {
        return new ResponseEntity<>(likeServiceImpl.getAllLikesByParameter(userId, postId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Like> createLike(@RequestBody CreateLikeRequest createLikeRequest) {
        return new ResponseEntity<>(likeServiceImpl.createLike(createLikeRequest), HttpStatus.OK);
    }

    @DeleteMapping("/post/{postId}/user/{userId}")
    public ResponseEntity<Void> deleteLike(@PathVariable("postId") Long postId, @PathVariable("userId") Long userId) {
        likeServiceImpl.deleteLikeByPostIdAndUserId(postId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/getLikersByPostId/{postId}/{userId}")
    ResponseEntity<List<FriendResponse>> getLikersByPostId(@PathVariable("postId") Long postId, @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(likeServiceImpl.getLikersByPostId(postId, userId));
    }
}
