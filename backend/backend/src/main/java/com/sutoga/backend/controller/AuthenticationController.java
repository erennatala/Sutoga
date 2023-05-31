package com.sutoga.backend.controller;

import com.sutoga.backend.entity.dto.AuthenticationResponse;
import com.sutoga.backend.entity.request.AuthenticationRequest;
import com.sutoga.backend.entity.request.RegisterRequest;
import com.sutoga.backend.service.AuthenticationService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthenticationController
{
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest registerRequest){
        return ResponseEntity.ok(authenticationService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest authenticationRequest){
        return ResponseEntity.ok(authenticationService.authenticate(authenticationRequest));
    }

    @PostMapping("/steamLogin/{steamId}")
    public ResponseEntity<AuthenticationResponse> steamLogin(@PathVariable Long steamId) {
        AuthenticationResponse user = authenticationService.handleSteamLogin(steamId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/checkIfSteamIdExists/{steamId}")
    public ResponseEntity<Boolean> checkIfSteamIdExists(@PathParam("steamId") Long steamId) {
        return ResponseEntity.ok(authenticationService.checkIfSteamIdExists(steamId));
    }

}
