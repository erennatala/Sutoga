package com.sutoga.backend.controller;

import com.sutoga.backend.entity.Like;
import com.sutoga.backend.entity.request.CreateLikeRequest;
import com.sutoga.backend.entity.response.LikeResponse;
import com.sutoga.backend.service.impl.LikeServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/likes")
@CrossOrigin(origins = "*")
public class LikeController {

    private LikeServiceImpl likeServiceImpl;

    public LikeController(LikeServiceImpl likeServiceImpl) {
        this.likeServiceImpl = likeServiceImpl;
    }

    @GetMapping
    public List<LikeResponse> getAllLikesByParameter(@RequestParam Optional<Long> userId,
                                                     @RequestParam Optional<Long> postId) {
        return likeServiceImpl.getAllLikesByParameter(userId, postId);
    }

    @PostMapping
    public Like createLike(@RequestBody CreateLikeRequest createLikeRequest) {
        return likeServiceImpl.createLike(createLikeRequest);
    }

    @DeleteMapping("/{likeId}")
    public void deleteLike(@RequestParam Long likeId) {
        likeServiceImpl.deleteLike(likeId);
    }


}
