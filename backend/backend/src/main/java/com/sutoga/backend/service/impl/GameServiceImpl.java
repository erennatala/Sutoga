package com.sutoga.backend.service.impl;

import com.sutoga.backend.entity.Game;
import com.sutoga.backend.entity.Like;
import com.sutoga.backend.entity.response.LikeResponse;
import com.sutoga.backend.repository.GameRepository;
import com.sutoga.backend.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;


    @Override
    public Game createGame(Game game) {
        return gameRepository.save(game);
    }


    @Override
    public Game getGameById(Long gameId) {
        return gameRepository.findById(gameId).orElse(null);
    }

    @Override
    public List<Game> getAllGamesByParameter(Optional<String> genreName, Optional<String> categoryName) {
        List<Game> gameList;
        if(categoryName.isPresent() && genreName.isPresent()){
            gameList=gameRepository.findByGenreNameAndCategoryName(genreName.get(), categoryName.get());
        } else if (genreName.isPresent()) {
            gameList = gameRepository.findByGenreName(genreName.get());
        } else if (categoryName.isPresent()) {
            gameList = gameRepository.findByCategoryName(categoryName.get());
        }
        else {
            gameList= gameRepository.findAll();
        }
        return  gameList;


    }

    @Override
    public void deleteGame(Long gameId) {
        gameRepository.deleteById(gameId);
    }
}
