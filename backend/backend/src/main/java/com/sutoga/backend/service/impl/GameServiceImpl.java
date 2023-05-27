package com.sutoga.backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sutoga.backend.controller.RecommendationApiClient;
import com.sutoga.backend.controller.SteamAPIService;
import com.sutoga.backend.entity.*;
import com.sutoga.backend.entity.response.GameResponse;
import com.sutoga.backend.entity.response.LikeResponse;
import com.sutoga.backend.entity.response.RecommendationResponse;
import com.sutoga.backend.repository.GameRepository;
import com.sutoga.backend.repository.RecommendationRepository;
import com.sutoga.backend.service.GameService;
import com.sutoga.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final UserService userService;
    private final SteamAPIService steamAPIService;
    private final RecommendationRepository recommendationRepository;

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
    public Page<GameResponse> getUserGames(Long userId, Pageable pageable) {
        User user = userService.getOneUserById(userId);
        if (user == null) {
            return new PageImpl<>(Collections.emptyList());
        }

        List<UserGame> userGames = user.getUserGames();
        if (userGames == null || userGames.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }

        List<GameResponse> gameResponses = userGames.stream()
                .map(this::mapToGameResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), gameResponses.size());
        Page<GameResponse> page = new PageImpl<>(gameResponses.subList(start, end), pageable, gameResponses.size());

        return page;
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

    @Override
    public void fetchUserGames(Long userId) {
        steamAPIService.fetchUserOwnedGames(userId);
    }

    @Override
    public List<RecommendationResponse> getRecommendationByUserId(Long userId) {
        User user = userService.getOneUserById(userId);
        RecommendationApiClient recommendationApiClient = new RecommendationApiClient("http://6.tcp.eu.ngrok.io:19689");
        String user_id = userId.toString();
        String recommendations = recommendationApiClient.getRecommendations(user_id);

        List<Long> existingGameIds = new ArrayList<>();
        List<Long> missingGameIds = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Parse recommendations
            JsonNode jsonNode = objectMapper.readTree(recommendations);
            ArrayNode recommendationArray = (ArrayNode) jsonNode.get("recommendations");

            List<RecommendationResponse> recommendationResponses = new ArrayList<>();

            for (JsonNode recommendationNode : recommendationArray) {
                Long appId = recommendationNode.get("appid").asLong();

                Game game = gameRepository.findByAppid(appId).orElse(null);

                if (game != null) {
                    Recommendation recommendation = new Recommendation();
                    recommendation.setUser(user); // Set the user ID
                    recommendation.setGame(game); // Set the game ID
                    // recommendation.setScore(score); // Set the similarity score
                    recommendationRepository.save(recommendation);

                    RecommendationResponse existingGameResponse = createRecommendationResponse(game);
                    recommendationResponses.add(existingGameResponse);
                    existingGameIds.add(appId);
                } else {
                    missingGameIds.add(appId);
                }
            }

            Thread apiThread = new Thread(() -> {
                List<RecommendationResponse> apiResponses = getGamesFromApi(user, recommendationArray);
                recommendationResponses.addAll(apiResponses);
                // Here you might want to send these responses back to the client,
                // or the client might need to poll the server to get these new games.
            });
            apiThread.start();

            try {
                apiThread.join(); // Wait for the thread to complete
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Return the games that we already have.
            return recommendationResponses;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    private RecommendationResponse createRecommendationResponse(Game game) {
        RecommendationResponse recommendationResponse = new RecommendationResponse();
        recommendationResponse.setId(game.getId());
        recommendationResponse.setGameTitle(game.getTitle());
        recommendationResponse.setGameDescription(game.getDescription());
        recommendationResponse.setGamePhotoUrl(game.getMediaUrl());
        recommendationResponse.setPublisher(game.getPublisher());
        recommendationResponse.setDeveloper(game.getDeveloper());
        LocalDate releaseDate = game.getReleaseDate();
        if (releaseDate != null) {
            recommendationResponse.setReleaseDate(releaseDate.toString());
        }
        //recommendationResponse.setScore(game.getScore());
        return recommendationResponse;
    }

    private List<RecommendationResponse> getGamesFromDatabase(User userId, ArrayNode recommendationArray) {
        List<RecommendationResponse> recommendationResponses = new ArrayList<>();
        for (JsonNode recommendationNode : recommendationArray) {
            Long appId = recommendationNode.get("appid").asLong();
            String score = recommendationNode.get("similarity_score").asText();
            Optional<Game> gameOptional = gameRepository.findById(appId);

            if (gameOptional.isPresent()) {
                Game game = gameOptional.get();
                RecommendationResponse recommendationResponse = mapGameToRecommendationResponse(game, score);
                recommendationResponses.add(recommendationResponse);

                // Save the recommendation to the database
                Recommendation recommendation = new Recommendation();
                recommendation.setUser(userId); // Set the user ID
                recommendation.setGame(game); // Set the game ID
                //recommendation.setScore(score); // Set the similarity score
                recommendationRepository.save(recommendation);
            }
        }
        return recommendationResponses;
    }

    private List<RecommendationResponse> getGamesFromApi(User user, ArrayNode recommendationArray) {
        List<RecommendationResponse> recommendationResponses = new ArrayList<>();
        List<CompletableFuture<RecommendationResponse>> futures = new ArrayList<>();

        for (JsonNode recommendationNode : recommendationArray) {
            Long appId = recommendationNode.get("appid").asLong();
            String score = recommendationNode.get("similarity_score").asText();
            Optional<Game> gameOptional = gameRepository.findById(appId);

            if (!gameOptional.isPresent()) {
                CompletableFuture<RecommendationResponse> future = steamAPIService.getGameDetailsObject(appId)
                        .flatMap(gameDetails -> {
                            if (gameDetails == null) {
                                return Mono.empty();
                            }
                            RecommendationResponse recommendationResponse = mapGameToRecommendationResponse(gameDetails, score);
                            Recommendation recommendation = new Recommendation();
                            recommendation.setUser(user); // Set the user ID
                            recommendation.setGame(gameDetails); // Set the game ID
                            //recommendation.setScore(score); // Set the similarity score
                            recommendationRepository.save(recommendation);
                            return Mono.just(recommendationResponse);
                        })
                        .toFuture();

                futures.add(future);
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .join();

        futures.forEach(future -> {
            try {
                RecommendationResponse recommendationResponse = future.get();
                if (recommendationResponse != null) {
                    recommendationResponses.add(recommendationResponse);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        return recommendationResponses;
    }

    public List<RecommendationResponse> getRecommendationsFromDatabase(Long userId) {
        List<Recommendation> recommendations = recommendationRepository.findByUserId(userId);

        List<RecommendationResponse> recommendationResponses = new ArrayList<>();

        for (Recommendation recommendation : recommendations) {
            Game game = recommendation.getGame();
            //String score = recommendation.get();
            RecommendationResponse recommendationResponse = mapGameToRecommendationResponse(game);
            recommendationResponses.add(recommendationResponse);

        }

        return recommendationResponses;
    }

    private RecommendationResponse mapGameToRecommendationResponse(Game game) {
        RecommendationResponse recommendationResponse = new RecommendationResponse();
        recommendationResponse.setGameTitle(game.getTitle());
        recommendationResponse.setGameDescription(game.getDescription());
        recommendationResponse.setGamePhotoUrl(game.getMediaUrl());
        recommendationResponse.setPublisher(game.getPublisher());
        recommendationResponse.setDeveloper(game.getDeveloper());
        if (game.getReleaseDate() != null) {
            recommendationResponse.setReleaseDate(game.getReleaseDate().toString());
        } else {
            recommendationResponse.setReleaseDate("N/A");
        }
        return recommendationResponse;
    }

    private RecommendationResponse mapGameToRecommendationResponse(Game game, String score) {
        RecommendationResponse recommendationResponse = new RecommendationResponse();
        recommendationResponse.setGameTitle(game.getTitle());
        recommendationResponse.setGameDescription(game.getDescription());
        recommendationResponse.setGamePhotoUrl(game.getMediaUrl());
        recommendationResponse.setPublisher(game.getPublisher());
        recommendationResponse.setDeveloper(game.getDeveloper());
        if (game.getReleaseDate() != null) {
            recommendationResponse.setReleaseDate(game.getReleaseDate().toString());
        } else {
            recommendationResponse.setReleaseDate("N/A");
        }
        recommendationResponse.setScore(score);
        return recommendationResponse;
    }

    private GameResponse mapToGameResponse(Game game) {
        GameResponse gameResponse = new GameResponse();
        gameResponse.setId(game.getId());
        gameResponse.setGameTitle(game.getTitle());
        gameResponse.setGameDescription(game.getDescription());
        gameResponse.setGamePhotoUrl(game.getMediaUrl());
        gameResponse.setPublisher(game.getPublisher());
        gameResponse.setDeveloper(game.getDeveloper());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String releaseDate = game.getReleaseDate() != null ? game.getReleaseDate().format(formatter) : "";
        gameResponse.setReleaseDate(releaseDate);

        return gameResponse;
    }


}
