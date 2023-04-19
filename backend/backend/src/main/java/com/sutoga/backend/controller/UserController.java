package com.sutoga.backend.controller;

import com.sutoga.backend.entity.FriendRequest;
import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.dto.UserResponse;
import com.sutoga.backend.exceptions.ResultNotFoundException;
import com.sutoga.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.sutoga.backend.entity.request.UpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;


    @GetMapping
    public UserResponse getOneUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getOneUserByEmail(userDetails.getUsername());
        if(user == null) {
            throw new ResultNotFoundException("User with email "+ userDetails.getUsername() +" not found!" );
        }
        return new UserResponse(user);
    }

    @PostMapping
    public ResponseEntity<Void> updateOneUser( @RequestBody UpdateRequest  newUser) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User userToUpdate = userService.getOneUserByEmail(userDetails.getUsername());
        User user = userService.updateUser(userToUpdate.getId(), newUser);
        if(user != null)
            return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @DeleteMapping
    public void deleteOneUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User userToDelete = userService.getOneUserByEmail(userDetails.getUsername());
        userService.deleteById(userToDelete.getId());
    }

    @GetMapping("/getProfilePhoto/{userId}")
    public ResponseEntity<?> getProfilePhoto(@PathVariable Long userId) {
        MultipartFile photo = userService.getProfilePhoto(userId);
        return ResponseEntity.status(HttpStatus.OK).body(photo);
    }

    @PostMapping("/saveProfilePhoto/{userId}")
    public ResponseEntity<?> saveProfilePhoto(@RequestParam("file") MultipartFile file, @PathVariable Long userId) {
        return null;
    }

    @PostMapping("/sendFriendRequest")
    public ResponseEntity<?> sendFriendRequest(@RequestParam("senderId") Long senderId, @RequestParam("receiverUsername") String receiverUsername) {
        userService.addFriend(senderId, receiverUsername);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/getFriendRequests")
    public ResponseEntity<List<FriendRequest>> getFriendRequests(@RequestParam("userId") Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAllRequestsByUserId(userId));
    }

    @GetMapping("/getFriendRecommendations")
    public ResponseEntity<List<String>> getFriendRecommendations(@RequestParam("userId") Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getFriendRecommendationsByUser(userId));
    }

    @PostMapping("/acceptFriendRequest")
    public ResponseEntity<?> acceptFriendRequest() {
        return null;
    }

    @PostMapping("/declineFriendRequest")
    public ResponseEntity<?> declineFriendRequest() {return null;}


}
