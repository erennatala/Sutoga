package com.sutoga.backend.service;

import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.dto.AuthenticationResponse;
import com.sutoga.backend.entity.request.AuthenticationRequest;
import com.sutoga.backend.entity.request.RegisterRequest;

public interface AuthenticationService {

    AuthenticationResponse register(RegisterRequest registerRequest);

    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);

    void saveUserToken(User user, String jwtToken);

    void revokeAllUserTokens(User user);
}