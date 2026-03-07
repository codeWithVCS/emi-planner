package com.emiplanner.service;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void register_shouldCreateUser() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alice")
                .phoneNumber(uniquePhone())
                .password("password123")
                .build();

        UserResponse response = authService.register(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("Alice");
        assertThat(userRepository.existsByPhoneNumber(request.getPhoneNumber())).isTrue();
    }

    @Test
    void register_shouldThrowWhenPhoneAlreadyExists() {
        String phone = uniquePhone();
        authService.register(RegisterRequest.builder()
                .name("Alice")
                .phoneNumber(phone)
                .password("password123")
                .build());

        RegisterRequest duplicate = RegisterRequest.builder()
                .name("Bob")
                .phoneNumber(phone)
                .password("password123")
                .build();

        assertThrows(DuplicateResourceException.class, () -> authService.register(duplicate));
    }

    @Test
    void login_shouldReturnJwtForValidCredentials() {
        String phone = uniquePhone();
        authService.register(RegisterRequest.builder()
                .name("Alice")
                .phoneNumber(phone)
                .password("password123")
                .build());

        LoginResponse response = authService.login(LoginRequest.builder()
                .phoneNumber(phone)
                .password("password123")
                .build());

        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getUser()).isNotNull();
        assertThat(jwtService.isTokenValid(response.getAccessToken())).isTrue();
    }

    @Test
    void login_shouldThrowForInvalidPassword() {
        String phone = uniquePhone();
        authService.register(RegisterRequest.builder()
                .name("Alice")
                .phoneNumber(phone)
                .password("password123")
                .build());

        LoginRequest wrongPassword = LoginRequest.builder()
                .phoneNumber(phone)
                .password("wrongPass123")
                .build();

        assertThrows(AuthenticationException.class, () -> authService.login(wrongPassword));
    }

    @Test
    void getCurrentUser_shouldReturnUser() {
        UserResponse registered = authService.register(RegisterRequest.builder()
                .name("Alice")
                .phoneNumber(uniquePhone())
                .password("password123")
                .build());

        UserResponse currentUser = authService.getCurrentUser(registered.getId());

        assertThat(currentUser.getId()).isEqualTo(registered.getId());
        assertThat(currentUser.getPhoneNumber()).isEqualTo(registered.getPhoneNumber());
    }

    @Test
    void getCurrentUser_shouldThrowWhenMissing() {
        assertThrows(ResourceNotFoundException.class, () -> authService.getCurrentUser(UUID.randomUUID()));
    }

    @Test
    void login_shouldThrowForInactiveUser() {
        String phone = uniquePhone();
        authService.register(RegisterRequest.builder()
                .name("Alice")
                .phoneNumber(phone)
                .password("password123")
                .build());

        User user = userRepository.findByPhoneNumber(phone).orElseThrow();
        user.setActive(false);
        userRepository.save(user);

        assertThrows(AuthenticationException.class, () -> authService.login(LoginRequest.builder()
                .phoneNumber(phone)
                .password("password123")
                .build()));
    }

    private String uniquePhone() {
        long value = Math.abs(System.nanoTime() % 1_000_000_0000L);
        return String.format("%010d", value);
    }
}
