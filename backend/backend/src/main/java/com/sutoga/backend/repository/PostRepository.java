package com.sutoga.backend.repository;

import com.sutoga.backend.entity.Post;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepository extends MongoRepository<Post, Long> {


}
