package com.sutoga.backend.repository;

import com.sutoga.backend.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepository extends JpaRepository<Post, Long> {


}
