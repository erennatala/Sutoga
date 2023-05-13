package com.sutoga.backend.controller;

import com.sutoga.backend.entity.Game;
import com.sutoga.backend.entity.response.LikeResponse;
import com.sutoga.backend.service.GameService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {

    private GameService gameService;

    @PostMapping
    public Game createGame(@RequestBody Game game) {
        return gameService.createGame(game);
    }

    @GetMapping
    public List<Game> getAllGamesByParameter(@RequestParam Optional<String> genreName,
                                                     @RequestParam Optional<String> categoryName) {
        return gameService.getAllGamesByParameter(genreName, categoryName);
    }

    @GetMapping("/{gameId}")
    public Game getGameById(@PathVariable Long gameId) {
        return gameService.getGameById(gameId);
    }



    @DeleteMapping("/{gameId}")
    public void deleteGame(@PathVariable Long gameId) {
        gameService.deleteGame(gameId);
    }
}
