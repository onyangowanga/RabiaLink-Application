package com.codewith.RabiaLinkApp.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String token;
    private String tokenType;
    private Long expiresIn;
    private LocalDateTime loginTime;
}
