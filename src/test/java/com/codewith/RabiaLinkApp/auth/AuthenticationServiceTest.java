package com.codewith.RabiaLinkApp.auth;

import com.codewith.RabiaLinkApp.auth.domain.User;
import com.codewith.RabiaLinkApp.auth.domain.UserRole;
import com.codewith.RabiaLinkApp.auth.dto.LoginRequest;
import com.codewith.RabiaLinkApp.auth.dto.LoginResponse;
import com.codewith.RabiaLinkApp.auth.dto.RegisterRequest;
import com.codewith.RabiaLinkApp.auth.repository.UserRepository;
import com.codewith.RabiaLinkApp.auth.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Authentication Service Tests")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("testuser@test.com");
        user.setPassword("hashedPassword");
        user.setRole(UserRole.STAFF);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        // Setup register request
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("testuser@test.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole(UserRole.STAFF);

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("Should register user successfully")
    void testRegisterUser_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        authenticationService.register(registerRequest);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject duplicate username registration")
    void testRegisterUser_DuplicateUsername_ShouldFail() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(RuntimeException.class,
            () -> authenticationService.register(registerRequest),
            "Username already exists");
    }

    @Test
    @DisplayName("Should reject duplicate email registration")
    void testRegisterUser_DuplicateEmail_ShouldFail() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("testuser@test.com")).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(RuntimeException.class,
            () -> authenticationService.register(registerRequest),
            "Email already exists");
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLogin_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);

        // When
        LoginResponse response = authenticationService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertNotNull(response.getToken());
        assertEquals("Bearer", response.getTokenType());
    }

    @Test
    @DisplayName("Should reject login with invalid password")
    void testLogin_InvalidPassword_ShouldFail() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class,
            () -> authenticationService.login(loginRequest),
            "Invalid username or password");
    }

    @Test
    @DisplayName("Should reject login for non-existent user")
    void testLogin_UserNotFound_ShouldFail() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        loginRequest.setUsername("nonexistent");

        // When & Then
        assertThrows(RuntimeException.class,
            () -> authenticationService.login(loginRequest),
            "User not found");
    }

    @Test
    @DisplayName("Should check user role correctly")
    void testHasRole_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        boolean hasRole = authenticationService.hasRole("testuser", UserRole.STAFF);

        // Then
        assertTrue(hasRole);
    }

    @Test
    @DisplayName("Should return false for incorrect role")
    void testHasRole_IncorrectRole() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        boolean hasRole = authenticationService.hasRole("testuser", UserRole.ADMIN);

        // Then
        assertFalse(hasRole);
    }

    @Test
    @DisplayName("Should identify admin user")
    void testIsAdmin_Success() {
        // Given
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setRole(UserRole.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        // When
        boolean isAdmin = authenticationService.isAdmin("admin");

        // Then
        assertTrue(isAdmin);
    }

    @Test
    @DisplayName("Should identify manager user")
    void testIsManager_Success() {
        // Given
        User managerUser = new User();
        managerUser.setUsername("manager");
        managerUser.setRole(UserRole.MANAGER);
        when(userRepository.findByUsername("manager")).thenReturn(Optional.of(managerUser));

        // When
        boolean isManager = authenticationService.isManager("manager");

        // Then
        assertTrue(isManager);
    }

    @Test
    @DisplayName("Should register user with PARTNER role")
    void testRegisterUser_PartnerRole_Success() {
        // Given
        registerRequest.setRole(UserRole.PARTNER);
        registerRequest.setPartnerId(1L);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        authenticationService.register(registerRequest);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
    }
}
