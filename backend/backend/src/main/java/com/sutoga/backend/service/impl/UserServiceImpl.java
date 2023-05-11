package com.sutoga.backend.service.impl;

import com.sutoga.backend.entity.FriendRequest;
import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.dto.AuthenticationResponse;
import com.sutoga.backend.entity.request.UpdateRequest;
import com.sutoga.backend.repository.FriendRequestRepository;
import com.sutoga.backend.repository.UserRepository;
import com.sutoga.backend.service.AuthenticationService;
import com.sutoga.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final AuthenticationService authenticationService;

    private final PasswordEncoder passwordEncoder;
    private final FriendRequestRepository friendRequestRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getOneUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public User getOneUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public User updateUser(Long userId, UpdateRequest updateRequest) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User foundUser = user.get();
            foundUser.setUsername(updateRequest.getUsername());
            foundUser.setPassword(updateRequest.getPassword());
            foundUser.setBirthDate(updateRequest.getBirthDate());
            foundUser.setFirstName(updateRequest.getFirstName());
            foundUser.setLastName(updateRequest.getLastName());
            foundUser.setEmail(updateRequest.getEmail());
            foundUser.setPhoneNumber(updateRequest.getPhoneNumber());
            userRepository.save(foundUser);
            return foundUser;
        } else
            return null;
    }


    @Override
    public void saveProfilePhoto(MultipartFile photo, Long userId) {

    }

    @Override
    public MultipartFile getProfilePhoto(Long userId) {
        return null;
    }

    @Override
    public void deleteById(Long userId) {
        try {
            userRepository.deleteById(userId);
        } catch (EmptyResultDataAccessException e) {
            System.out.println("User " + userId + " doesn't exist");
        }
    }

    @Override
    public User getOneUserByUserName(String userName) {
        return userRepository.findByUsername(userName);
    }

    @Override
    public Boolean changePassword(String oldPassword, String newPassword, Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return false;
        }

        try {

        } catch (Exception e) {

        }
        return null;
    }

    @Override
    public Boolean connectSteam(Long userId) {
        return null;
    }

    @Override
    public List<User> getAllFriends(Long userId) {
        return userRepository.findById(userId).get().getFriends();
    }

    @Override
    public Boolean addFriend(Long userId, String receiverUsername) {
        FriendRequest friendRequest = new FriendRequest();

        User sender = userRepository.findById(userId).orElse(null);
        User receiver = userRepository.findByUsername(receiverUsername);

        friendRequest.setSender(sender);
        friendRequest.setReceiver(receiver);

        friendRequestRepository.save(friendRequest);
        return true;
    }

    @Override
    public Boolean acceptFriendRequest(Long requestId) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId).orElse(null);
        User sender = friendRequest.getSender();
        User receiver = friendRequest.getReceiver();

        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);

        friendRequestRepository.delete(friendRequest);
        return true;
    }

    @Override
    public Boolean declineFriendRequest(Long requestId) {
        friendRequestRepository.delete(Objects.requireNonNull(friendRequestRepository.findById(requestId).orElse(null)));
        return true;
    }

    @Override
    public List<String> getFriendRecommendationsByUser(Long userId) {
        List<User> userFriends = userRepository.findById(userId).orElse(null).getFriends();
        List<Long> friendIds = new ArrayList<>();

        userFriends.forEach(user -> friendIds.add(user.getId()));
        friendIds.add(userId);

        List<User> recommendations = userRepository.findRandomUsersExcludingIds(friendIds, 3);

        List<String> usernames = new ArrayList<>();

        recommendations.forEach(user -> usernames.add(user.getUsername()));

        return usernames;
    }

    @Override
    public List<FriendRequest> getAllRequestsByUserId(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return friendRequestRepository.findFriendRequestByReceiver(user);
    }

    @Override
    public Boolean removeFriend(Long userId, Long friendId) {
        return null;
    }

}
