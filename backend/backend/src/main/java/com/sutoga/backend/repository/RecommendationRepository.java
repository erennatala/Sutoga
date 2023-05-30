package com.sutoga.backend.repository;

import com.sutoga.backend.entity.Recommendation;
import com.sutoga.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByUserId(Long userId);
    void deleteByUser(User user);
}
