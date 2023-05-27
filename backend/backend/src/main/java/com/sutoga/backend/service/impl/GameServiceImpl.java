package com.sutoga.backend.service.impl;

import com.sutoga.backend.entity.Game;
import com.sutoga.backend.entity.Like;
import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.UserGame;
import com.sutoga.backend.entity.response.GameResponse;
import com.sutoga.backend.entity.response.LikeResponse;
import com.sutoga.backend.repository.GameRepository;
import com.sutoga.backend.service.GameService;
import com.sutoga.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final UserService userService;

//    @Override
//    public List<Game> getAllGamesByParameter(Optional<String> genreName, Optional<String> categoryName) {
//        List<Game> gameList;
//        if(categoryName.isPresent() && genreName.isPresent()){
//            gameList=gameRepository.findByGenreNameAndCategoryName(genreName.get(), categoryName.get());
//        } else if (genreName.isPresent()) {
//            gameList = gameRepository.findByGenreName(genreName.get());
//        } else if (categoryName.isPresent()) {
//            gameList = gameRepository.findByCategoryName(categoryName.get());
//        }
//        else {
//            gameList= gameRepository.findAll();
//        }
//        return  gameList;
//    }

    @Override
    public void deleteGame(Long gameId) {
        gameRepository.deleteById(gameId);
    }

    @Override
    public List<GameResponse> getUserGames(Long userId) {
        User user = userService.getOneUserById(userId);
        if (user == null) {
            return new ArrayList<>();
        }

        List<UserGame> userGames = user.getUserGames();
        if (userGames == null || userGames.isEmpty()) {
            return new ArrayList<>();
        }

        return userGames.stream()
                .map(this::mapToGameResponse)
                .filter(Objects::nonNull) // Null olmayanları filtrele
                .collect(Collectors.toList());
    }

    private GameResponse mapToGameResponse(UserGame userGame) {
        if (userGame.getGame() == null) {
            return null; // veya uygun bir hata işleme stratejisi
        }

        Game game = userGame.getGame();

        GameResponse gameResponse = new GameResponse();
        gameResponse.setId(game.getId());
        gameResponse.setGameTitle(game.getTitle());
        gameResponse.setGameDescription(game.getDescription());
        gameResponse.setPlaytime(userGame.getPlayTime());
        gameResponse.setGamePhotoUrl(game.getMediaUrl());
        gameResponse.setPublisher(game.getPublisher());
        gameResponse.setDeveloper(game.getDeveloper());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String releaseDate = game.getReleaseDate() != null ? game.getReleaseDate().format(formatter) : "";
        gameResponse.setReleaseDate(releaseDate);

        return gameResponse;
    }

    @Override
    public Integer getUserGameCount(Long userId) {
        User user = userService.getOneUserById(userId);
        if (user == null) {
            return 0;
        }

        List<UserGame> userGames = user.getUserGames();
        if (userGames == null || userGames.isEmpty()) {
            return 0;
        }

        return userGames.size();
    }

    @Override
    public Integer getUserGameCountByUsername(String username) {
        User user = userService.getOneUserByUserName(username);
        if (user == null) {
            return 0;
        }

        List<UserGame> userGames = user.getUserGames();
        if (userGames == null || userGames.isEmpty()) {
            return 0;
        }

        return userGames.size();
    }

}
