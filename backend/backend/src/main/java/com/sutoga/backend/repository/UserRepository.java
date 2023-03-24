package com.sutoga.backend.repository;

import com.sutoga.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String userName);
}
