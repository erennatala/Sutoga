package com.sutoga.backend.repository;

import com.sutoga.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String userName);

    Optional<User> findByEmail(String email);
}
