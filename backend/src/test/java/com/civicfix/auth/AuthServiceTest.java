package com.civicfix.auth;

import com.civicfix.audit.AuditLogService;
import com.civicfix.common.BadRequestException;
import com.civicfix.security.JwtTokenProvider;
import com.civicfix.user.Role;
import com.civicfix.user.User;
import com.civicfix.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private AuditLogService auditLogService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                authenticationManager,
                userRepository,
                passwordEncoder,
                tokenProvider,
                auditLogService
        );
    }

    @Test
    @DisplayName("Public registration strictly creates CITIZEN role and hashes password")
    void testRegistrationCreatesCitizenWithHashedPassword() {
        RegisterRequest req = new RegisterRequest("Test Citizen", "new@test.com", "secret123", "+919999999999");

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$hashedPasswordMock");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(101L);
            return u;
        });
        when(tokenProvider.generateTokenFromUser(any())).thenReturn("mock.jwt.token");

        AuthResponse res = authService.register(req);

        assertNotNull(res);
        assertEquals("mock.jwt.token", res.getAccessToken());
        assertEquals("new@test.com", res.getUser().getEmail());
        assertEquals(Role.CITIZEN, res.getUser().getRole());

        verify(passwordEncoder, times(1)).encode("secret123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Registration with existing email throws BadRequestException")
    void testRegistrationDuplicateEmailRejected() {
        RegisterRequest req = new RegisterRequest("Duplicate User", "existing@test.com", "pass123", null);
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }
}
