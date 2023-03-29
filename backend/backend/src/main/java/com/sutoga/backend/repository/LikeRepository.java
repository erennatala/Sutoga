package com.sutoga.backend.repository;

import com.sutoga.backend.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Long> {


    List<Like> findByUserId(Long userId);

    List<Like> findByPostId(Long postId);

    List<Like> findByUserIdAndPostId(Long userId, Long postId);


}
