package com.civicfix.user;

import com.civicfix.audit.AuditLogService;
import com.civicfix.common.BadRequestException;
import com.civicfix.common.PagedResponse;
import com.civicfix.common.ResourceNotFoundException;
import com.civicfix.department.Department;
import com.civicfix.department.DepartmentRepository;
import com.civicfix.security.SecurityUtils;
import com.civicfix.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository,
                       DepartmentRepository departmentRepository,
                       PasswordEncoder passwordEncoder,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserDto> getAllUsers(Role role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> paged = (role != null)
                ? userRepository.findByRole(role, pageable)
                : userRepository.findAll(pageable);

        List<UserDto> dtoList = paged.getContent().stream().map(UserDto::fromEntity).toList();
        return new PagedResponse<>(dtoList, paged.getNumber(), paged.getSize(), paged.getTotalElements(), paged.getTotalPages(), paged.isLast());
    }

    @Transactional(readOnly = true)
    public List<UserDto> getWorkers(Long departmentId) {
        List<User> workers = (departmentId != null)
                ? userRepository.findByDepartmentIdAndRole(departmentId, Role.WORKER)
                : userRepository.findByRole(Role.WORKER);

        return workers.stream().map(UserDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserDto.fromEntity(user);
    }

    @Transactional
    public UserDto createUser(String email, String rawPassword, String fullName, String phone, Role role, Long departmentId) {
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("An account with email " + normalizedEmail + " already exists");
        }

        Department department = null;
        if (departmentId != null) {
            department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));
        }

        User user = new User(
                normalizedEmail,
                passwordEncoder.encode(rawPassword),
                fullName.trim(),
                phone,
                role != null ? role : Role.CITIZEN,
                department
        );

        User saved = userRepository.save(user);

        UserPrincipal adminUser = SecurityUtils.getCurrentUser();
        auditLogService.logAction("ADMIN_CREATE_USER", "User", saved.getId().toString(), adminUser.getId(), adminUser.getUsername(), adminUser.getRole().name(), "Created user account: " + saved.getEmail() + " with role: " + saved.getRole());

        return UserDto.fromEntity(saved);
    }

    @Transactional
    public UserDto updateUserRole(Long userId, Role newRole, Long departmentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Role oldRole = user.getRole();
        user.setRole(newRole);

        if (departmentId != null) {
            Department dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));
            user.setDepartment(dept);
        } else if (newRole == Role.CITIZEN || newRole == Role.ADMIN) {
            user.setDepartment(null);
        }

        User saved = userRepository.save(user);

        UserPrincipal adminUser = SecurityUtils.getCurrentUser();
        auditLogService.logAction("UPDATE_USER_ROLE", "User", saved.getId().toString(), adminUser.getId(), adminUser.getUsername(), adminUser.getRole().name(), String.format("Changed role of %s from %s to %s", saved.getEmail(), oldRole, newRole));

        return UserDto.fromEntity(saved);
    }

    @Transactional
    public UserDto toggleUserActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setActive(active);
        User saved = userRepository.save(user);

        UserPrincipal adminUser = SecurityUtils.getCurrentUser();
        auditLogService.logAction("TOGGLE_USER_ACTIVE", "User", saved.getId().toString(), adminUser.getId(), adminUser.getUsername(), adminUser.getRole().name(), (active ? "Activated" : "Deactivated") + " user: " + saved.getEmail());

        return UserDto.fromEntity(saved);
    }

    @Transactional
    public UserDto updateProfile(Long userId, String fullName, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (StringUtils.hasText(fullName)) {
            user.setFullName(fullName.trim());
        }
        if (phone != null) {
            user.setPhone(phone.trim());
        }

        User saved = userRepository.save(user);
        return UserDto.fromEntity(saved);
    }
}
