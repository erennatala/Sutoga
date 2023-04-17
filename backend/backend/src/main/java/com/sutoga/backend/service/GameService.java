package com.sutoga.backend.service;

import com.sutoga.backend.entity.Game;
import com.sutoga.backend.entity.response.LikeResponse;

import java.util.List;
import java.util.Optional;

public interface GameService {

    Game createGame(Game game);

    List<Game> getAllGamesByParameter(Optional<String> genreName, Optional<String> categoryName);

    Game getGameById(Long gameId);


    void deleteGame(Long gameId);

}
