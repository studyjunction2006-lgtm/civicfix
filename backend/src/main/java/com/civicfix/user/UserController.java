package com.civicfix.user;

import com.civicfix.common.ApiResponse;
import com.civicfix.security.SecurityUtils;
import com.civicfix.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Endpoints for user profiles and worker queries")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/workers")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    @Operation(summary = "Get list of available workers (filterable by department)")
    public ResponseEntity<ApiResponse<List<UserDto>>> getWorkers(
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getWorkers(departmentId)));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update authenticated user's profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(@RequestBody Map<String, String> body) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        String fullName = body.get("fullName");
        String phone = body.get("phone");

        UserDto updated = userService.updateProfile(principal.getId(), fullName, phone);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", updated));
    }
}
