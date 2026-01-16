package com.codewith.RabiaLinkApp.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.codewith.RabiaLinkApp.auth.domain.UserRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private UserRole role;
    private Long partnerId;  // Optional, for PARTNER role
}
