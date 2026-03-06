package com.emiplanner.service;

import com.emiplanner.dto.auth.LoginRequest;
import com.emiplanner.dto.auth.LoginResponse;
import com.emiplanner.dto.auth.RegisterRequest;
import com.emiplanner.dto.user.UserResponse;

import java.util.UUID;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserResponse getCurrentUser(UUID userId);

}
