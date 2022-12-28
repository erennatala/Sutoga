package com.sutoga.backend.controller;

import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.dto.UserDto;
import com.sutoga.backend.service.UserService;
import com.sutoga.backend.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {

    private final UserServiceImpl userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers()); // 200
    }

    @PostMapping(value = "/signup")
    public ResponseEntity<UserDto>
}
