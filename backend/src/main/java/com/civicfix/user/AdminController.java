package com.civicfix.user;

import com.civicfix.audit.AuditLogService;
import com.civicfix.common.ApiResponse;
import com.civicfix.common.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administration", description = "Endpoints for platform administration, user management, and audit inspection")
public class AdminController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    public AdminController(UserService userService, AuditLogService auditLogService) {
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/users")
    @Operation(summary = "Get paginated user list with optional role filter (Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<UserDto>>> getUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getAllUsers(role, page, size)));
    }

    public static class AdminCreateUserRequest {
        @NotBlank @Email private String email;
        @NotBlank private String password;
        @NotBlank private String fullName;
        private String phone;
        @NotNull private Role role;
        private Long departmentId;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    }

    @PostMapping("/users")
    @Operation(summary = "Create an authority, worker, or administrator user (Admin only)")
    public ResponseEntity<ApiResponse<UserDto>> createUser(@RequestBody AdminCreateUserRequest req) {
        UserDto user = userService.createUser(
                req.getEmail(), req.getPassword(), req.getFullName(), req.getPhone(), req.getRole(), req.getDepartmentId()
        );
        return new ResponseEntity<>(ApiResponse.ok("User created successfully", user), HttpStatus.CREATED);
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Update user role and department assignment (Admin only)")
    public ResponseEntity<ApiResponse<UserDto>> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        String roleStr = (String) body.get("role");
        Role role = Role.valueOf(roleStr);
        Long deptId = body.get("departmentId") != null ? Long.valueOf(body.get("departmentId").toString()) : null;

        UserDto updated = userService.updateUserRole(id, role, deptId);
        return ResponseEntity.ok(ApiResponse.ok("User role updated successfully", updated));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Activate or deactivate a user account (Admin only)")
    public ResponseEntity<ApiResponse<UserDto>> toggleUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {

        boolean active = body.getOrDefault("active", true);
        UserDto updated = userService.toggleUserActive(id, active);
        return ResponseEntity.ok(ApiResponse.ok("User account status updated", updated));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get platform audit logs with pagination (Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogService.AuditLogDto>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(auditLogService.getAuditLogs(page, size)));
    }
}
