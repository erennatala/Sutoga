package com.sutoga.backend.service;

import com.sutoga.backend.entity.Game;
import com.sutoga.backend.entity.response.GameResponse;
import com.sutoga.backend.entity.response.LikeResponse;

import java.util.List;
import java.util.Optional;

public interface GameService {
    void deleteGame(Long gameId);
    List<GameResponse> getUserGames(Long userId);
    Integer getUserGameCount(Long userId);
    Integer getUserGameCountByUsername(String username);
}
