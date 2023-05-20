package com.sutoga.backend.repository;

import com.sutoga.backend.entity.Post;
import com.sutoga.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByUser(User user, Pageable pageable);
    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId")
    int findPostCountByUserId(@Param("userId") Long userId);

}
