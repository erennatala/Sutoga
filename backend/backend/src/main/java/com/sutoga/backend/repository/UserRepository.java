package com.sutoga.backend.repository;

import com.sutoga.backend.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepository extends MongoRepository<User, Long> {
    List<User> findUserByUsername(String username);
    List<User> findUserByEmail(String email);
}
