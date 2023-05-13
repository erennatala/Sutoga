package com.sutoga.backend.service.impl;

import com.sutoga.backend.entity.Like;
import com.sutoga.backend.entity.Post;
import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.request.CreateLikeRequest;
import com.sutoga.backend.entity.response.LikeResponse;
import com.sutoga.backend.exceptions.ResultNotFoundException;
import com.sutoga.backend.repository.LikeRepository;
import com.sutoga.backend.service.LikeService;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class LikeServiceImpl implements LikeService {

    private LikeRepository likeRepository;
    private UserServiceImpl userServiceImpl;
    private PostServiceImpl postServiceImpl;

    public LikeServiceImpl(LikeRepository likeRepository, UserServiceImpl userServiceImpl, PostServiceImpl postServiceImpl) {
        this.likeRepository = likeRepository;
        this.userServiceImpl = userServiceImpl;
        this.postServiceImpl = postServiceImpl;
    }

    @Override
    public Like createLike(CreateLikeRequest createLikeRequest) {
//        User user = userServiceImpl.getOneUserById(createLikeRequest.getUserId());
//
//        if(user!=null && post!=null){
//            Like like = new Like();
//            like.setUser(user);
//            like.setPost(post);
//
//            return likeRepository.save(like);
//        }
//        else{
//            throw new ResultNotFoundException("Invalid userId or postId");
//        }
        return null;
    }

    @Override
    public List<LikeResponse> getAllLikesByParameter(Optional<Long> userId, Optional<Long> postId) {
        List<Like> likeList;
        if(userId.isPresent() && postId.isPresent()){
            likeList=likeRepository.findByUserIdAndPostId(userId.get(),postId.get());
        } else if (userId.isPresent()) {
            likeList = likeRepository.findByUserId(userId.get());
        } else if (postId.isPresent()) {
            likeList = likeRepository.findByPostId(postId.get());
        }
        else {
            likeList= likeRepository.findAll();
        }
        return likeList.stream().map(like ->
            new LikeResponse(like.getId(), like.getUser().getId(), like.getPost().getId())).collect(Collectors.toList());


    }

    @Override
    public void deleteLike(Long id) {
        likeRepository.deleteById(id);
    }


}
