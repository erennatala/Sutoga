package com.sutoga.backend.controller;

import com.sutoga.backend.entity.Game;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/steam")
public class SteamAPIController {

    private final SteamAPIService steamAPIService;

    public SteamAPIController(SteamAPIService steamAPIService) {
        this.steamAPIService = steamAPIService;
    }

    @PostMapping("/fetchAllGames")
    public ResponseEntity<Void> fetchAllGames() {
        steamAPIService.fetchTop100Games();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/games")
    public ResponseEntity<List<Game>> getAllGames() {
        List<Game> games = steamAPIService.getAllGames();
        return ResponseEntity.ok(games);
    }

    @PostMapping("/fetchUserGames/{userId}")
    public ResponseEntity<Boolean> fetchUserGames(@PathVariable Long userId) {
        return ResponseEntity.ok(steamAPIService.fetchUserOwnedGames(userId));
    }

    // ...other endpoints

}