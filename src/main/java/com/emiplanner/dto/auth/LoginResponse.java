package com.emiplanner.dto.auth;

import com.emiplanner.dto.user.UserResponse;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class LoginResponse {

    private String accessToken;

    private String tokenType = "Bearer";

    private long expiresIn;

    private UserResponse user;

}
