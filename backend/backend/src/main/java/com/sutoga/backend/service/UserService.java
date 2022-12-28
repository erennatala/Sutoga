package com.sutoga.backend.service;

import com.sutoga.backend.entity.dto.UserDto;
import com.sutoga.backend.entity.request.LoginRequest;
import com.sutoga.backend.entity.request.SignUpRequest;

import java.util.List;

public interface UserService {

    LoginRequest signIn(LoginRequest loginRequest);
    Long signUp(SignUpRequest signUpRequest);
    UserDto getUserById(Long userId);
    void deleteById(Long userId);
    int numberOfUsers();
    List<UserDto> getAllUsers();
    UserDto getUserByUsername(String username);
}
