package com.sutoga.backend.controller;

import com.sutoga.backend.entity.dto.AuthResponse;
import com.sutoga.backend.entity.request.LoginRequest;
import com.sutoga.backend.entity.request.RefreshRequest;
import com.sutoga.backend.entity.request.RegisterRequest;
import com.sutoga.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    @PostMapping(value = "/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest newUser) {
        AuthResponse response = userService.signUp(newUser);
        if(response != null)
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        final AuthResponse loginResponse = userService.login(loginRequest);

        if(loginResponse != null) {
            return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    @PostMapping(value = "/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest refreshRequest) {
        AuthResponse response = userService.refresh(refreshRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}