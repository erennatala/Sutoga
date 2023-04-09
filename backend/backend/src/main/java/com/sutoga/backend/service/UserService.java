package com.sutoga.backend.service;

import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.dto.AuthResponse;
import com.sutoga.backend.entity.request.LoginRequest;
import com.sutoga.backend.entity.request.RefreshRequest;
import com.sutoga.backend.entity.request.RegisterRequest;
import com.sutoga.backend.entity.request.UpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    User updateUser(Long userId, UpdateRequest updateRequest);
    Boolean changePassword(String oldPassword, String newPassword, Long userId);
    Boolean connectSteam(Long userId);
    List<User> getAllFriends(Long userId);
    Boolean addFriend(Long userId, Long friendId);
    Boolean removeFriend(Long userId, Long friendId);
    User getOneUserByUserName(String userName);
    void deleteById(Long userId);
    User getOneUserById(Long userId);
    List<User> getAllUsers();
    AuthResponse signUp(RegisterRequest newUser);
    AuthResponse login(LoginRequest loginRequest);
    AuthResponse refresh(RefreshRequest refreshRequest);
    void saveProfilePhoto(MultipartFile photo, Long userId);
    MultipartFile getProfilePhoto(Long userId);
}
