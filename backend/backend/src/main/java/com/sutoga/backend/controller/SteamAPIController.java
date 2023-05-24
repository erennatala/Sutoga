package com.sutoga.backend.controller;

import com.sutoga.backend.entity.Game;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // ...other endpoints

}