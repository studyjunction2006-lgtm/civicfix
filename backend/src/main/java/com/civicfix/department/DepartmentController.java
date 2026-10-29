package com.civicfix.department;

import com.civicfix.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@Tag(name = "Departments", description = "Endpoints for campus departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @Operation(summary = "List all active departments (Public)")
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> getAllDepartments() {
        return ResponseEntity.ok(ApiResponse.ok(departmentService.getAllDepartments()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by ID (Public)")
    public ResponseEntity<ApiResponse<DepartmentDto>> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(departmentService.getDepartmentById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new department (Admin only)")
    public ResponseEntity<ApiResponse<DepartmentDto>> createDepartment(@Valid @RequestBody DepartmentDto dto) {
        DepartmentDto created = departmentService.createDepartment(dto);
        return new ResponseEntity<>(ApiResponse.ok("Department created", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing department (Admin only)")
    public ResponseEntity<ApiResponse<DepartmentDto>> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentDto dto) {
        DepartmentDto updated = departmentService.updateDepartment(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Department updated", updated));
    }
}
