package com.codewith.RabiaLinkApp.auth.service;

import com.codewith.RabiaLinkApp.auth.domain.User;
import com.codewith.RabiaLinkApp.auth.domain.UserRole;
import com.codewith.RabiaLinkApp.auth.dto.LoginRequest;
import com.codewith.RabiaLinkApp.auth.dto.LoginResponse;
import com.codewith.RabiaLinkApp.auth.dto.RegisterRequest;
import com.codewith.RabiaLinkApp.auth.dto.RegisterResponse;
import com.codewith.RabiaLinkApp.auth.repository.UserRepository;
import com.codewith.RabiaLinkApp.auth.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register a new user
     */
    public RegisterResponse register(RegisterRequest request) {
        // Validate username doesn't exist
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Validate email doesn't exist
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : UserRole.STAFF);
        user.setPartnerId(request.getPartnerId());

        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().toString())
                .createdAt(savedUser.getCreatedAt())
                .message("User registered successfully")
                .build();
    }

    /**
     * Authenticate user and return JWT token
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user);

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400L)  // 24 hours
                .loginTime(LocalDateTime.now())
                .build();
    }

    /**
     * Get user by ID
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Verify token validity
     */
    public boolean isTokenValid(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * Get username from token
     */
    public String getUsernameFromToken(String token) {
        return jwtTokenProvider.getUsernameFromToken(token);
    }

    /**
     * Check if user has specific role
     */
    public boolean hasRole(String username, UserRole role) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getRole() == role;
    }

    /**
     * Check if user is admin
     */
    public boolean isAdmin(String username) {
        return hasRole(username, UserRole.ADMIN);
    }

    /**
     * Check if user is manager
     */
    public boolean isManager(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getRole() == UserRole.MANAGER || user.getRole() == UserRole.ADMIN;
    }

    /**
     * Get current user role from token
     */
    public UserRole getCurrentUserRole(String token) {
        String roleString = jwtTokenProvider.getRoleFromToken(token);
        return UserRole.valueOf(roleString);
    }

    /**
     * Get current user's partner ID (for PARTNER role)
     */
    public Long getCurrentUserPartnerId(String token) {
        return jwtTokenProvider.getPartnerIdFromToken(token);
    }
}
