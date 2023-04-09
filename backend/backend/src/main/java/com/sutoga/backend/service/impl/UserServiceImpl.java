package com.sutoga.backend.service.impl;

import com.sutoga.backend.config.security.JwtTokenProvider;
import com.sutoga.backend.entity.RefreshToken;
import com.sutoga.backend.entity.User;
import com.sutoga.backend.entity.dto.AuthResponse;
import com.sutoga.backend.entity.request.LoginRequest;
import com.sutoga.backend.entity.request.RefreshRequest;
import com.sutoga.backend.entity.request.RegisterRequest;
import com.sutoga.backend.entity.request.UpdateRequest;
import com.sutoga.backend.repository.UserRepository;
import com.sutoga.backend.service.RefreshTokenService;
import com.sutoga.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    @Override
    public AuthResponse signUp(RegisterRequest registerRequest) {
        AuthResponse authResponse = new AuthResponse();
        if(getOneUserByUserName(registerRequest.getUsername()) != null) {
            authResponse.setMessage("Username already in use.");
            return authResponse;
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setGender(registerRequest.getGender());
        userRepository.save(user);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(registerRequest.getUsername(), registerRequest.getPassword());
        Authentication auth = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwtToken = jwtTokenProvider.generateJwtToken(auth);

        authResponse.setMessage("User successfully registered.");
        authResponse.setAccessToken("Bearer " + jwtToken);
        authResponse.setRefreshToken(refreshTokenService.createRefreshToken(user));
        authResponse.setUserId(user.getId());

        return authResponse;
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        User user = getOneUserByUserName(loginRequest.getUsername());

        if (user == null) {
            return null;
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
        Authentication auth = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwtToken = jwtTokenProvider.generateJwtToken(auth);
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken("Bearer " + jwtToken);
        authResponse.setRefreshToken(refreshTokenService.createRefreshToken(user));
        authResponse.setUserId(user.getId());
        authResponse.setMessage("Succesfull login.");

        return authResponse;
    }

    @Override
    public User getOneUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public User updateUser(Long userId, UpdateRequest updateRequest) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()) {
            User foundUser = user.get();
            foundUser.setUsername(updateRequest.getUsername());
            foundUser.setPassword(updateRequest.getPassword());
            foundUser.setGender(updateRequest.getGender());
            foundUser.setBirthDate(updateRequest.getBirthDate());
            foundUser.setFirstName(updateRequest.getFirstName());
            foundUser.setLastName(updateRequest.getLastName());
            foundUser.setEmail(updateRequest.getEmail());
            foundUser.setPhoneNumber(updateRequest.getPhoneNumber());
            userRepository.save(foundUser);
            return foundUser;
        }else
            return null;
    }

    @Override
    public AuthResponse refresh(RefreshRequest refreshRequest) {
        AuthResponse response = new AuthResponse();
        RefreshToken token = refreshTokenService.getByUser(refreshRequest.getUserId());
        if(token.getToken().equals(refreshRequest.getRefreshToken()) &&
                !refreshTokenService.isRefreshExpired(token)) {

            User user = token.getUser();
            String jwtToken = jwtTokenProvider.generateJwtTokenByUserId(user.getId());
            response.setMessage("token successfully refreshed.");
            response.setAccessToken("Bearer " + jwtToken);
            response.setUserId(user.getId());
            return response;
        } else {
            response.setMessage("refresh token is not valid.");
            return response;
        }
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
        }catch(EmptyResultDataAccessException e) {
            System.out.println("User "+userId+" doesn't exist");
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
    public Boolean addFriend(Long userId, Long friendId) {
        return null;
    }

    @Override
    public Boolean removeFriend(Long userId, Long friendId) {
        return null;
    }


}
