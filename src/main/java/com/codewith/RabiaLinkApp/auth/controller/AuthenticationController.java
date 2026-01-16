package com.codewith.RabiaLinkApp.auth.controller;

import com.codewith.RabiaLinkApp.auth.dto.LoginRequest;
import com.codewith.RabiaLinkApp.auth.dto.LoginResponse;
import com.codewith.RabiaLinkApp.auth.dto.RegisterRequest;
import com.codewith.RabiaLinkApp.auth.dto.RegisterResponse;
import com.codewith.RabiaLinkApp.auth.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        RegisterResponse response = authenticationService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Login and get JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify token validity
     */
    @GetMapping("/verify-token")
    public ResponseEntity<Boolean> verifyToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        boolean isValid = authenticationService.isTokenValid(token);
        return ResponseEntity.ok(isValid);
    }

    /**
     * Get current user info (requires authentication)
     */
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String username = authenticationService.getUsernameFromToken(token);
        return ResponseEntity.ok("Current user: " + username);
    }
}
