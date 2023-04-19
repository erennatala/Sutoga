package com.sutoga.backend.service;

import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.request.UpdateRequest;

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

    User getOneUserByEmail(String email);
}
