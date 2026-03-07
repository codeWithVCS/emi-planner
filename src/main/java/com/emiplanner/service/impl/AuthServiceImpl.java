package com.emiplanner.service.impl;

import com.emiplanner.dto.auth.LoginRequest;
import com.emiplanner.dto.auth.LoginResponse;
import com.emiplanner.dto.auth.RegisterRequest;
import com.emiplanner.dto.user.UserResponse;
import com.emiplanner.entity.User;
import com.emiplanner.exception.AuthenticationException;
import com.emiplanner.exception.DuplicateResourceException;
import com.emiplanner.exception.ResourceNotFoundException;
import com.emiplanner.repository.UserRepository;
import com.emiplanner.security.JwtService;
import com.emiplanner.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${jwt.expiration}")
    private long expiration;

    @Override
    public UserResponse register(RegisterRequest request) {
        log.info("Register request received for phoneNumber={}", request.getPhoneNumber());

        if(userRepository.existsByPhoneNumber(request.getPhoneNumber())){
            log.warn("Register failed: phoneNumber already exists, phoneNumber={}", request.getPhoneNumber());
            throw new DuplicateResourceException("Provided Phone Number is already in use");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPasswordHash(passwordHash);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully. userId={}, phoneNumber={}", savedUser.getId(), savedUser.getPhoneNumber());

        return mapToUserResponse(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for phoneNumber={}", request.getPhoneNumber());

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found for phoneNumber={}", request.getPhoneNumber());
                    return new AuthenticationException("Invalid credentials");
                });

        if(!user.isActive()){
            log.warn("Login failed: inactive user. userId={}", user.getId());
            throw new AuthenticationException("Invalid credentials");
        }

        if(!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            log.warn("Login failed: password mismatch. userId={}", user.getId());
            throw new AuthenticationException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId());

        LoginResponse response = new LoginResponse();
        response.setAccessToken(token);
        response.setExpiresIn(expiration);
        response.setUser(mapToUserResponse(user));
        log.info("Login successful. userId={}", user.getId());

        return response;
    }

    @Override
    public UserResponse getCurrentUser(UUID userId) {
        log.info("Fetching current user details for userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Get current user failed: user not found for userId={}", userId);
                    return new ResourceNotFoundException("User not found");
                });
        log.info("Current user details fetched successfully for userId={}", userId);

        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
