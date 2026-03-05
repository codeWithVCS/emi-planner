package com.emiplanner.dto.user;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserResponse {

    private UUID id;
    private String name;
    private String phoneNumber;
    private LocalDateTime createdAt;

}
