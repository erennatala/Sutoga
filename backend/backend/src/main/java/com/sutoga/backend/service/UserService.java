package com.sutoga.backend.service;

import com.sutoga.backend.entity.Notification;
import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.dto.UserResponse;
import com.sutoga.backend.entity.request.UpdateRequest;
import com.sutoga.backend.entity.response.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    UserResponse updateUser(Long userId, UpdateRequest updateRequest);
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
    List<FriendRequestResponse> getUnconfirmedFriendRequestsByUserId(Long userId);
    Boolean areFriends(Long userId1, Long userId2);
    FriendRequestResponse checkFriendRequest(Long userId, Long accountId);
    String getProfilePhotoUrlByUsername(String username);
    FriendRecResponse getFriendRecommendationByUser(Long userId);
    List<FriendResponse> getFriendsByUserId(Long userId, int page, int size);
    List<FriendResponse> getFriendsByUsername(String username, Long userId, int page, int size);

    List<ChatFriendResponse> getFriendsByUsernameForChat(String username);
    Integer getPostCountByUserId(Long userId);
    Integer getGameCountByUserId(Long userId);
    Integer getFriendCountByUserId(Long userId);
    Boolean changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword);
    Boolean checkUsername(String username);
    Boolean checkEmail(String email);
    List<Notification> getNotification(Long userId);
    Boolean checkSteamId(Long userId);
    Boolean removeFriendRequest(Long userId, String username);
    Boolean areFriendsByUsername(Long userId, String username2);
    FriendRequestResponse checkFriendRequestByUsername(Long userId, String username);
    Boolean removeFriendByUsername(Long userId, String friendUsername);
    Boolean checkIfSteamIdExists(Long steamId);
    Boolean connectSteamForGames(Long userId, Long steamId);
}
