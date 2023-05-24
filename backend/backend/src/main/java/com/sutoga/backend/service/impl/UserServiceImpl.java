package com.sutoga.backend.service.impl;

import com.amazonaws.services.cognitoidp.model.InvalidPasswordException;
import com.sutoga.backend.entity.FriendRequest;
import com.sutoga.backend.entity.Notification;
import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.UserFriend;
import com.sutoga.backend.entity.dto.UserResponse;
import com.sutoga.backend.entity.mapper.UserMapper;
import com.sutoga.backend.entity.request.UpdateRequest;
import com.sutoga.backend.entity.response.FriendRecResponse;
import com.sutoga.backend.entity.response.FriendRequestResponse;
import com.sutoga.backend.entity.response.FriendResponse;
import com.sutoga.backend.entity.response.UserSearchResponse;
import com.sutoga.backend.exceptions.ResultNotFoundException;
import com.sutoga.backend.repository.FriendRequestRepository;
import com.sutoga.backend.repository.NotificationRepository;
import com.sutoga.backend.repository.PostRepository;
import com.sutoga.backend.repository.UserRepository;
import com.sutoga.backend.service.AuthenticationService;
import com.sutoga.backend.service.UserService;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
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
    private final PostRepository postRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

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
    public UserResponse updateUser(Long userId, UpdateRequest updateRequest) {
        Optional<User> user = userRepository.findById(userId);
        if (userRepository.existsByUsername(updateRequest.getUsername()) && !Objects.equals(user.get().getUsername(), updateRequest.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (userRepository.existsByEmail(updateRequest.getEmail()) && !Objects.equals(user.get().getEmail(), updateRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already taken");
        }
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
            return new UserResponse(foundUser);
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
        User user = userRepository.findById(userId).get();
        List<UserFriend> userFriends = user.getUserFriends();
        return userFriends.stream()
                .map(UserFriend::getFriend)
                .collect(Collectors.toList());
    }


    @Override
    public Boolean addFriend(Long userId, String receiverUsername) {
        FriendRequest friendRequest = new FriendRequest();

        User sender = userRepository.findById(userId).orElse(null);
        User receiver = userRepository.findByUsername(receiverUsername);

        FriendRequest existingFriendRequest = friendRequestRepository.findBySenderAndReceiver(sender, receiver);
        if (existingFriendRequest != null) {
            return false;
        }

        friendRequest.setSender(sender);
        friendRequest.setReceiver(receiver);
        friendRequest.setCreatedAt(LocalDateTime.now());
        friendRequest.setConfirmed(false);

        friendRequestRepository.save(friendRequest);

        Notification notification = new Notification();
        notification.setReceiver(receiver);
        notification.setFriendRequestActivity(friendRequest);
        notification.setSeen(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setSenderUsername(sender.getUsername());
        notification.setSenderPhotoUrl(sender.getProfilePhotoUrl());

        // Save and send notification
        notificationService.createAndSendNotification(notification);

        return true;
    }

    @Override
    public List<FriendRecResponse> getFriendRecommendationsByUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        List<UserFriend> userFriendEntities = user.getUserFriends();
        List<Long> friendIds = new ArrayList<>();
        List<User> friends = new ArrayList<>();

        userFriendEntities.forEach(userFriend -> {
            friendIds.add(userFriend.getFriend().getId());
            friends.add(userFriend.getFriend());
        });
        friendIds.add(userId);

        List<FriendRequest> existingFriendRequests = friendRequestRepository.findBySenderAndReceiverIn(user, friends);

        List<Long> excludedUserIds = existingFriendRequests.stream()
                .map(friendRequest -> friendRequest.getReceiver().getId())
                .collect(Collectors.toList());

        excludedUserIds.addAll(friendIds);

        List<User> recommendations = userRepository.findRandomUsersExcludingIds(excludedUserIds, 3);

        List<FriendRecResponse> recs = new ArrayList<>();

        recommendations.forEach(user2 -> {
            FriendRecResponse rec = new FriendRecResponse();
            rec.setUsername(user2.getUsername());
            rec.setProfilePhotoUrl(user2.getProfilePhotoUrl());

            recs.add(rec);
        });

        return recs;
    }

    @Override
    public FriendRecResponse getFriendRecommendationByUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        List<UserFriend> userFriends = user.getUserFriends();
        List<User> friends = userFriends.stream().map(UserFriend::getFriend).collect(Collectors.toList());
        List<Long> friendIds = friends.stream().map(User::getId).collect(Collectors.toList());
        friendIds.add(userId);

        List<FriendRequest> existingFriendRequests = friendRequestRepository.findBySenderAndReceiverIn(user, friends); //TODO BURDA HATA OLABİLİR

        List<Long> excludedUserIds = existingFriendRequests.stream()
                .map(friendRequest -> friendRequest.getReceiver().getId())
                .collect(Collectors.toList());
        excludedUserIds.addAll(friendIds);

        User recommendation = userRepository.findRandomUserExcludingIds(excludedUserIds);

        FriendRecResponse rec = new FriendRecResponse();
        rec.setUsername(recommendation.getUsername());
        rec.setProfilePhotoUrl(recommendation.getProfilePhotoUrl());

        return rec;
    }

    public List<FriendResponse> getFriendsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User user = userRepository.findById(userId).orElse(null);

        List<User> friends = user.getUserFriends().stream().map(UserFriend::getFriend).collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), friends.size());

        if (start > end) {
            start = 0;
        }

        List<User> friendsSlice = new ArrayList<>(friends.subList(start, end));

        List<FriendResponse> friendsResponse = new ArrayList<>();

        friendsSlice.forEach(friend -> {
            FriendResponse userResponse = new FriendResponse();

            userResponse.setProfilePhotoUrl(friend.getProfilePhotoUrl());
            userResponse.setUsername(friend.getUsername());
            userResponse.setId(friend.getId());
            userResponse.setIsFriend(true);

            friendsResponse.add(userResponse);
        });

        return friendsResponse;
    }

    @Override
    public List<FriendResponse> getFriendsByUsername(String username, Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User user = userRepository.findByUsername(username);

        User appUser = userRepository.findById(userId).orElse(null);

        List<User> friends = user.getUserFriends().stream().map(UserFriend::getFriend).collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), friends.size());
        List<User> friendsSlice = new ArrayList<>(friends.subList(start, end));

        List<FriendResponse> friendsResponse = new ArrayList<>();

        friendsSlice.forEach(friend -> {
            FriendResponse userResponse = new FriendResponse();

            userResponse.setProfilePhotoUrl(friend.getProfilePhotoUrl());
            userResponse.setUsername(friend.getUsername());
            userResponse.setId(friend.getId());
            userResponse.setIsFriend(areFriends(friend.getId(), appUser.getId()) || friend.getId() == appUser.getId());

            friendsResponse.add(userResponse);
        });

        return friendsResponse;
    }

    @Override
    public Integer getPostCountByUserId(Long userId) {
        return postRepository.findPostCountByUserId(userId);
    }

    @Override
    public Integer getFriendCountByUserId(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResultNotFoundException("User not found")).getUserFriends().size();
    }

    @Override
    public Integer getGameCountByUserId(Long userId) {
        return null;
    }

    @Override
    public Boolean removeFriend(Long userId, Long friendId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResultNotFoundException("User not found"));
        User friend = userRepository.findById(friendId).orElseThrow(() -> new ResultNotFoundException("Friend not found"));

        Optional<UserFriend> userFriend = user.getUserFriends().stream()
                .filter(uf -> uf.getFriend().getId().equals(friendId))
                .findFirst();

        if (userFriend.isPresent()) {
            user.getUserFriends().remove(userFriend.get());
            userRepository.save(user);
            return true;
        }

        return false;
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

        System.out.println(users);

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
    public List<FriendRequestResponse> getUnconfirmedFriendRequestsByUserId(Long userId) {
        List<FriendRequest> unconfirmedRequests = friendRequestRepository.findByReceiverIdAndConfirmedFalse(userId);
        unconfirmedRequests.sort(Comparator.comparing(FriendRequest::getCreatedAt).reversed());

        // Convert entity list to DTO list
        return unconfirmedRequests.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private FriendRequestResponse convertToDto(FriendRequest friendRequest) {
        FriendRequestResponse dto = new FriendRequestResponse();
        dto.setId(friendRequest.getId());
        dto.setSenderId(friendRequest.getSender().getId());
        dto.setReceiverId(friendRequest.getReceiver().getId());

        FriendRecResponse sender = new FriendRecResponse();
        sender.setUsername(friendRequest.getSender().getUsername());
        sender.setProfilePhotoUrl(friendRequest.getSender().getProfilePhotoUrl());
        dto.setSender(sender);

        FriendRecResponse receiver = new FriendRecResponse();
        receiver.setUsername(friendRequest.getReceiver().getUsername());
        receiver.setProfilePhotoUrl(friendRequest.getReceiver().getProfilePhotoUrl());
        dto.setReceiver(receiver);

        return dto;
    }


    @Override
    public Boolean acceptFriendRequest(Long requestId) {
        Optional<FriendRequest> friendRequestOptional = friendRequestRepository.findById(requestId);
        if (friendRequestOptional.isPresent()) {
            FriendRequest friendRequest = friendRequestOptional.get();
            friendRequest.setConfirmed(true);
            User sender = friendRequest.getSender();
            User receiver = friendRequest.getReceiver();

            Notification notification = notificationRepository.findByFriendRequestActivity(friendRequest);

            if(notification != null) {
                notification.setFriendRequestActivity(null);
                notificationRepository.save(notification);
            }

            friendRequestRepository.delete(friendRequest);
            if (notification != null) {
                notificationRepository.delete(notification);
            }

            UserFriend userFriend1 = new UserFriend(null, sender, receiver);
            UserFriend userFriend2 = new UserFriend(null, receiver, sender);

            sender.getUserFriends().add(userFriend1);
            receiver.getUserFriends().add(userFriend2);

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

            Notification notification = notificationRepository.findByFriendRequestActivity(friendRequest);

            if(notification != null) {
                notification.setFriendRequestActivity(null);
                notificationRepository.save(notification);
            }

            friendRequestRepository.delete(friendRequest);
            if (notification != null) {
                notificationRepository.delete(notification);
            }
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

        List<User> friendsOfUser1 = user1.getUserFriends().stream().map(UserFriend::getFriend).collect(Collectors.toList());
        List<User> friendsOfUser2 = user2.getUserFriends().stream().map(UserFriend::getFriend).collect(Collectors.toList());

        return friendsOfUser1.contains(user2) && friendsOfUser2.contains(user1);
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
            friendRequest = friendRequestRepository.findBySenderAndReceiver(receiver, sender);

            if (friendRequest != null) {
                FriendRequestResponse response = new FriendRequestResponse();
                response.setId(friendRequest.getId());
                response.setSenderId(friendRequest.getSender().getId());
                response.setReceiverId(friendRequest.getReceiver().getId());
                return response;
            }

            else {
                return null;
            }
        }
    }

    @Override
    public String getProfilePhotoUrlByUsername(String username) {
        return userRepository.findByUsername(username).getProfilePhotoUrl();
    }

    @Transactional
    public Boolean changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResultNotFoundException("User with id " + userId + " not found!"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidPasswordException("Invalid current password!");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new InvalidPasswordException("New password and confirm password do not match!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return true;
    }

    @Override
    public Boolean checkUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Boolean checkEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<Notification> getNotification(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        List<Notification> notifications = notificationService.getNotifications(user);
        notifications.sort(Comparator.comparing(Notification::getCreatedAt).reversed());

        return notifications;
    }

}
