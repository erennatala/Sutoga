package com.sutoga.backend.service;

import com.sutoga.backend.entity.FriendRequest;
import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.dto.AuthenticationResponse;
import com.sutoga.backend.entity.dto.UserResponse;
import com.sutoga.backend.entity.request.UpdateRequest;
import com.sutoga.backend.entity.response.FriendRecResponse;
import com.sutoga.backend.entity.response.FriendRequestResponse;
import com.sutoga.backend.entity.response.UserSearchResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    User updateUser(Long userId, UpdateRequest updateRequest);
    Boolean changePassword(String oldPassword, String newPassword, Long userId);
    Boolean connectSteam(Long userId);
    List<User> getAllFriends(Long userId);
    Boolean addFriend(Long userId, String receiverUsername);
    Boolean acceptFriendRequest(Long requestId);
    Boolean declineFriendRequest(Long requestId);
    List<FriendRecResponse> getFriendRecommendationsByUser(Long userId);
    Boolean removeFriend(Long userId, Long friendId);
    User getOneUserByUserName(String userName);
    void deleteById(Long userId);
    User getOneUserById(Long userId);
    List<User> getAllUsers();
    void saveProfilePhoto(MultipartFile photo, Long userId);
    MultipartFile getProfilePhoto(Long userId);
    User getOneUserByEmail(String email);
    String getProfilePhotoUrl(Long userId);
    List<UserSearchResponse> searchUsers(String query);
    UserResponse getUserByUsername(String username);
    List<FriendRequest> getUnconfirmedFriendRequestsByUserId(Long userId);
    Boolean areFriends(Long userId1, Long userId2);
    FriendRequestResponse checkFriendRequest(Long userId, Long accountId);
    String getProfilePhotoUrlByUsername(String username);
    FriendRecResponse getFriendRecommendationByUser(Long userId);
}
