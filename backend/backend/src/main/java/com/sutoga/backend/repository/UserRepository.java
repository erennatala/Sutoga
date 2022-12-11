package com.sutoga.backend.repository;

import com.sutoga.backend.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, Long> {
}
