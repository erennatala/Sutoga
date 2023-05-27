package com.sutoga.backend.controller;

import com.sutoga.backend.entity.Notification;
import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.dto.UserResponse;
import com.sutoga.backend.entity.response.*;
import com.sutoga.backend.exceptions.ResultNotFoundException;
import com.sutoga.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sutoga.backend.entity.request.UpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

//    @GetMapping
//    public List<UserResponse> getAllUsers(){
//        return userService.getAllUsers().stream().map(UserResponse::new).collect(Collectors.toList());
//    }

    @GetMapping("/{userId}")
    public UserResponse getOneUser(@PathVariable Long userId) {
        User user = userService.getOneUserById(userId);
        if(user == null) {
            throw new ResultNotFoundException("User with id "+ userId +" not found!" );
        }
        return new UserResponse(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateOneUser(@PathVariable Long userId,
                                              @RequestParam("email") String email,
                                              @RequestParam("username") String username,
                                              @RequestParam("description") String description,
                                              @RequestParam("firstName") String firstName,
                                              @RequestParam("lastName") String lastName,
                                              @RequestParam("phoneNumber") String phoneNumber,
                                              @RequestParam("birthDate") LocalDate birthDate,
                                              @RequestParam(value = "media", required = false) MultipartFile media) {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setEmail(email);
        updateRequest.setUsername(username);
        updateRequest.setDescription(description);
        updateRequest.setFirstName(firstName);
        updateRequest.setLastName(lastName);
        updateRequest.setPhoneNumber(phoneNumber);
        updateRequest.setBirthDate(birthDate);
        updateRequest.setMedia(media);

        UserResponse user = userService.updateUser(userId, updateRequest);

        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @DeleteMapping("/{userId}")
    public void deleteOneUser(@PathVariable Long userId) {
        userService.deleteById(userId);
    }

    @PostMapping("/saveProfilePhoto/{userId}")
    public ResponseEntity<?> saveProfilePhoto(@RequestParam("file") MultipartFile file, @PathVariable Long userId) {
        return null;
    }

    @PostMapping("/sendFriendRequest")
    public ResponseEntity<Boolean> sendFriendRequest(@RequestParam("senderId") Long senderId, @RequestParam("receiverUsername") String receiverUsername) {
        Boolean isSent = userService.addFriend(senderId, receiverUsername);
        if (Boolean.TRUE.equals(isSent)) {
            return new ResponseEntity<>(true, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getFriendRecommendations")
    public ResponseEntity<List<FriendRecResponse>> getFriendRecommendations(@RequestParam("userId") Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getFriendRecommendationsByUser(userId));
    }

    @GetMapping("/getFriendRecommendation")
    public ResponseEntity<FriendRecResponse> getFriendRecommendation(@RequestParam("userId") Long userId) {
        FriendRecResponse recommendation = userService.getFriendRecommendationByUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(recommendation);
    }

    @GetMapping("/getProfilePhoto/{userId}")
    public ResponseEntity<String> getProfilePhoto(@PathVariable Long userId) {
        return new ResponseEntity<>(userService.getProfilePhotoUrl(userId), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResponse>> searchUsers(@RequestParam("q") String query) {
        List<UserSearchResponse> users = userService.searchUsers(query);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/getByUsername/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        UserResponse user = userService.getUserByUsername(username);
        if (user == null) {
            throw new ResultNotFoundException("User with username " + username + " not found!");
        }
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @GetMapping("/unconfirmed")
    public ResponseEntity<List<FriendRequestResponse>> getUnconfirmedFriendRequests(@RequestParam("userId") Long userId) {
        List<FriendRequestResponse> unconfirmedRequests = userService.getUnconfirmedFriendRequestsByUserId(userId);
        return new ResponseEntity<>(unconfirmedRequests, HttpStatus.OK);
    }

    @PostMapping("/acceptFriendRequest/{requestId}")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable Long requestId) {
        if (userService.acceptFriendRequest(requestId)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/declineFriendRequest/{requestId}")
    public ResponseEntity<?> declineFriendRequest(@PathVariable Long requestId) {
        if (userService.declineFriendRequest(requestId)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/areFriends")
    public ResponseEntity<Boolean> areFriends(@RequestParam("userId1") Long userId1, @RequestParam("userId2") Long userId2) {
        boolean areFriends = userService.areFriends(userId1, userId2);
        return ResponseEntity.ok(areFriends);
    }

    @PostMapping("/checkFriendRequest")
    public ResponseEntity<FriendRequestResponse> checkFriendRequest(@RequestParam("userId") Long userId,
                                                                    @RequestParam("accountId") Long accountId) {
        FriendRequestResponse friendRequestResponse = userService.checkFriendRequest(userId, accountId);
        return ResponseEntity.ok(friendRequestResponse);
    }

    @GetMapping("/profilePhoto")
    public ResponseEntity<String> getProfilePhotoByUsername(@RequestParam("username") String username) {
        return ResponseEntity.ok(userService.getProfilePhotoUrlByUsername(username));
    }

    @GetMapping("/getFriendsByUserId")
    public ResponseEntity<List<FriendResponse>> getFriendsByUserId(@RequestParam("userId") Long userId,
                                                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                                                   @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getFriendsByUserId(userId, page, size));
    }

    @GetMapping("/getFriendsByUsername")
    public ResponseEntity<List<FriendResponse>> getFriendsByUsername(@RequestParam("username") String username,
                                                                   @RequestParam("userId") Long userId,
                                                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                                                   @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getFriendsByUsername(username, userId, page, size));
    }


    @GetMapping("/getFriendsByUsernameForChat")
    public ResponseEntity<List<ChatFriendResponse>> getFriendsByUsernameForChat(@RequestParam("username") String username)
                                                                     {
        return ResponseEntity.ok(userService.getFriendsByUsernameForChat(username));
    }

    @GetMapping("/getPostCount/{userId}")
    public ResponseEntity<Integer> getPostCount(@PathVariable("userId") Long userId){
        return ResponseEntity.ok(userService.getPostCountByUserId(userId));
    }

    @GetMapping("/getFriendCount/{userId}")
    public ResponseEntity<Integer> getFriendCount(@PathVariable("userId") Long userId){
        return ResponseEntity.ok(userService.getFriendCountByUserId(userId));
    }

    @GetMapping("/getGameCount/{userId}")
    public ResponseEntity<Integer> getGameCount(@PathVariable("userId") Long userId){
        return ResponseEntity.ok(userService.getGameCountByUserId(userId));
    }

    @PostMapping("/changePassword/{userId}")
    public ResponseEntity<?> changePassword(@PathVariable("userId") Long userId,
                                            @RequestParam("currentPassword") String currentPassword,
                                            @RequestParam("newPassword") String newPassword,
                                            @RequestParam("confirmPassword") String confirmPassword) {
        Boolean isPasswordChanged = userService.changePassword(userId, currentPassword, newPassword, confirmPassword);
        if (isPasswordChanged) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/notifications/{userId}")
    public ResponseEntity<List<Notification> > getUserNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getNotification(userId));
    }

    @DeleteMapping("/{userId}/remove/{friendId}")
    public ResponseEntity<?> removeFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        Boolean isRemoved = userService.removeFriend(userId, friendId);
        if (Boolean.TRUE.equals(isRemoved)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{userId}/removeByUsername/{friendUsername}")
    public ResponseEntity<?> removeFriendByUsername(@PathVariable Long userId, @PathVariable String friendUsername) {
        Boolean isRemoved = userService.removeFriendByUsername(userId, friendUsername);
        if (Boolean.TRUE.equals(isRemoved)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/checkSteamId/{userId}")
    public ResponseEntity<?> checkSteamId(@PathVariable Long userId) {
        if (Boolean.TRUE.equals(userService.checkSteamId(userId))) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{userId}/removeRequest/{username}")
    public ResponseEntity<?> removeFriendRequest(@PathVariable Long userId, @PathVariable String username) {
        Boolean isRemoved = userService.removeFriendRequest(userId, username);
        if (Boolean.TRUE.equals(isRemoved)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/areFriendsByUsername")
    public ResponseEntity<Boolean> areFriends(@RequestParam("userId1") Long userId1, @RequestParam("username") String username) {
        boolean areFriends = userService.areFriendsByUsername(userId1, username);
        return ResponseEntity.ok(areFriends);
    }

    @PostMapping("/checkFriendRequestByUsername")
    public ResponseEntity<FriendRequestResponse> checkFriendRequest(@RequestParam("userId") Long userId,
                                                                    @RequestParam("username") String username) {
        FriendRequestResponse friendRequestResponse = userService.checkFriendRequestByUsername(userId, username);
        return ResponseEntity.ok(friendRequestResponse);
    }

}
