package com.sutoga.backend.service.impl;

import com.sutoga.backend.entity.FriendRequest;
import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.dto.UserResponse;
import com.sutoga.backend.entity.mapper.UserMapper;
import com.sutoga.backend.entity.request.UpdateRequest;
import com.sutoga.backend.entity.response.FriendRequestResponse;
import com.sutoga.backend.entity.response.UserSearchResponse;
import com.sutoga.backend.exceptions.ResultNotFoundException;
import com.sutoga.backend.repository.FriendRequestRepository;
import com.sutoga.backend.repository.UserRepository;
import com.sutoga.backend.service.AuthenticationService;
import com.sutoga.backend.service.UserService;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final AuthenticationService authenticationService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FriendRequestRepository friendRequestRepository;

    @Value("${aws.s3.bucket-name}")
    private String s3BucketName;

    @Value("${aws.cloudfront.domain-name}")
    private String cloudFrontDomainName;

    private final MinioClient minioClient;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getOneUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResultNotFoundException("User with id "+ userId +" not found!" ));
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
            foundUser.setBirthDate(updateRequest.getBirthDate());
            foundUser.setFirstName(updateRequest.getFirstName());
            foundUser.setLastName(updateRequest.getLastName());
            foundUser.setEmail(updateRequest.getEmail());
            foundUser.setPhoneNumber(updateRequest.getPhoneNumber());
            foundUser.setProfileDescription(updateRequest.getDescription());

            if(updateRequest.getMedia() != null) {
                String profilePhotoUrl = uploadMediaToMinioAndGenerateUrl(updateRequest.getMedia());

                foundUser.setProfilePhotoUrl(profilePhotoUrl);
            }

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
    public Boolean removeFriend(Long userId, Long friendId) {
        return null;
    }

    public User handleProfilePictureUpload(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId).orElseThrow(null);

        // Upload the profile picture to the MinIO server and generate the URL
        String profilePhotoUrl = uploadMediaToMinioAndGenerateUrl(file);

        // Add the media URL to the profilePhotoUrl field in the User entity
        user.setProfilePhotoUrl(profilePhotoUrl);

        // Save the updated User entity to the database
        return userRepository.save(user);
    }

    private String generateUniqueMediaName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID().toString() + extension;
    }

    public InputStream getMediaAsStream(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(s3BucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving media from MinIO server", e);
        }
    }

    public String uploadMediaToMinioAndGenerateUrl(MultipartFile file) {
        String objectName = generateUniqueMediaName(file.getOriginalFilename());
        String bucketName = s3BucketName;

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            String mediaUrl = cloudFrontDomainName + "/" + bucketName + "/" + objectName;
            return mediaUrl;

        } catch (Exception e) {
            throw new RuntimeException("Error uploading media to MinIO server", e);
        }
    }

    public String getProfilePhotoUrl(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        assert user != null;
        return user.getProfilePhotoUrl();
    }

    public List<UserSearchResponse> searchUsers(String query) {
        String lowerCaseQuery = query.toLowerCase();
        List<User> users = userRepository.findAll();

        return users.stream()
                .filter(user -> user.getUsername().toLowerCase().contains(lowerCaseQuery))
                .map(user -> {
                    UserSearchResponse userResponse = new UserSearchResponse();
                    userResponse.setUsername(user.getUsername());
                    userResponse.setProfilePhotoUrl(user.getProfilePhotoUrl());
                    return userResponse;
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return userMapper.toUserResponse(user);
        } else {
            return null;
        }
    }

    @Override
    public List<FriendRequest> getUnconfirmedFriendRequestsByUserId(Long userId) {
        List<FriendRequest> unconfirmedRequests = friendRequestRepository.findByReceiverIdAndConfirmedFalse(userId);
        unconfirmedRequests.sort(Comparator.comparing(FriendRequest::getCreatedAt).reversed());

        return unconfirmedRequests;
    }

    @Override
    public Boolean acceptFriendRequest(Long requestId) {
        Optional<FriendRequest> friendRequestOptional = friendRequestRepository.findById(requestId);
        if (friendRequestOptional.isPresent()) {
            FriendRequest friendRequest = friendRequestOptional.get();
            friendRequest.setConfirmed(true);
            friendRequestRepository.save(friendRequest);
            User sender = friendRequest.getSender();
            User receiver = friendRequest.getReceiver();
            sender.getFriends().add(receiver);
            receiver.getFriends().add(sender);
            userRepository.save(sender);
            userRepository.save(receiver);
            return true;
        }
        return false;
    }

    @Override
    public Boolean declineFriendRequest(Long requestId) {
        Optional<FriendRequest> friendRequestOptional = friendRequestRepository.findById(requestId);
        if (friendRequestOptional.isPresent()) {
            FriendRequest friendRequest = friendRequestOptional.get();
            friendRequestRepository.delete(friendRequest);
            return true;
        }
        return false;
    }
    @Override
    public Boolean areFriends(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new ResultNotFoundException("User with ID " + userId1 + " not found"));
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new ResultNotFoundException("User with ID " + userId2 + " not found"));

        return user1.getFriends().contains(user2) && user2.getFriends().contains(user1);
    }

    @Override
    public FriendRequestResponse checkFriendRequest(Long userId, Long accountId) {
        User sender = userRepository.findById(userId).orElseThrow(() -> new ResultNotFoundException("User with id " + userId + " not found"));
        User receiver = userRepository.findById(accountId).orElseThrow(() -> new ResultNotFoundException("User with id " + accountId + " not found"));

        FriendRequest friendRequest = friendRequestRepository.findBySenderAndReceiver(sender, receiver);

        if (friendRequest != null) {
            FriendRequestResponse response = new FriendRequestResponse();
            response.setId(friendRequest.getId());
            response.setSenderId(friendRequest.getSender().getId());
            response.setReceiverId(friendRequest.getReceiver().getId());
            return response;
        } else {
            return null;
        }
    }




}
