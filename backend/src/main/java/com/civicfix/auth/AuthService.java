package com.civicfix.auth;

import com.civicfix.audit.AuditLogService;
import com.civicfix.common.BadRequestException;
import com.civicfix.security.JwtTokenProvider;
import com.civicfix.security.SecurityUtils;
import com.civicfix.security.UserPrincipal;
import com.civicfix.user.Role;
import com.civicfix.user.User;
import com.civicfix.user.UserDto;
import com.civicfix.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditLogService auditLogService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuditLogService auditLogService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadRequestException("User record not found"));

        auditLogService.logAction("USER_LOGIN", "User", user.getId().toString(), user.getId(), user.getEmail(), user.getRole().name(), "User successfully logged in");

        return new AuthResponse(jwt, UserDto.fromEntity(user));
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("An account with email " + normalizedEmail + " already exists");
        }

        // Public self-registration ALWAYS creates a CITIZEN account for security
        User user = new User(
                normalizedEmail,
                passwordEncoder.encode(request.getPassword()),
                request.getFullName().trim(),
                request.getPhone() != null ? request.getPhone().trim() : null,
                Role.CITIZEN,
                null
        );

        User savedUser = userRepository.save(user);

        UserPrincipal principal = UserPrincipal.create(savedUser);
        String jwt = tokenProvider.generateTokenFromUser(principal);

        auditLogService.logAction("USER_REGISTER", "User", savedUser.getId().toString(), savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name(), "Citizen account self-registered");

        return new AuthResponse(jwt, UserDto.fromEntity(savedUser));
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUserDto() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User profile not found"));
        return UserDto.fromEntity(user);
    }
}
