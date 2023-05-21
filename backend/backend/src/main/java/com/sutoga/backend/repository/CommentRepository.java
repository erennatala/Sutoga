package com.sutoga.backend.repository;

import com.sutoga.backend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);
    Page<Comment> findAllByPostId(Long postId, Pageable pageable);
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    Integer getCommentCountByPostId(@Param("postId") Long postId);
}

