package com.sutoga.backend.service;

import com.sutoga.backend.entity.Game;
import com.sutoga.backend.entity.Recommendation;
import com.sutoga.backend.entity.response.GameResponse;
import com.sutoga.backend.entity.response.LikeResponse;
import com.sutoga.backend.entity.response.RecommendationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

public interface GameService {
    void deleteGame(Long gameId);
    Page<GameResponse> getUserGames(Long userId, Pageable pageable);
    Integer getUserGameCount(Long userId);
    Integer getUserGameCountByUsername(String username);
    @Async
    void fetchUserGames(Long userId);
    List<RecommendationResponse> getRecommendationByUserId(Long userId);
    List<RecommendationResponse> getRecommendationsFromDatabase(Long userId);
}
