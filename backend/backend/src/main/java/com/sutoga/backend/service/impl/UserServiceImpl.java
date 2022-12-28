package com.sutoga.backend.service.impl;

import com.sutoga.backend.entity.dto.UserDto;
import com.sutoga.backend.entity.mapper.UserMapper;
import com.sutoga.backend.entity.request.LoginRequest;
import com.sutoga.backend.entity.request.SignUpRequest;
import com.sutoga.backend.repository.UserRepository;
import com.sutoga.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private UserMapper userMapper;

    @Override
    public LoginRequest signIn(LoginRequest loginRequest) {
        return null;
    }

    @Override
    public Long signUp(SignUpRequest signUpRequest) {
        return null;
    }

    @Override
    public UserDto getUserById(Long userId) {
        return userMapper.userToUserDto(userRepository.findById(userId).orElse(null));
    }

    @Override
    public void deleteById(Long userId) {
        return;
    }

    @Override
    public int numberOfUsers() {
        return 0;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userMapper.userToUserDtoList(userRepository.findAll());
    }

    @Override
    public UserDto getUserByUsername(String username) {
        return null;
    }
}
