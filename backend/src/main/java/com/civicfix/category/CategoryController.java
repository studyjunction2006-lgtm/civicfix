package com.civicfix.category;

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
@RequestMapping("/api/v1/categories")
@Tag(name = "Issue Categories", description = "Endpoints for issue categorization and SLA definitions")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "List all issue categories (Public)")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories(
            @RequestParam(required = false) Long departmentId) {
        if (departmentId != null) {
            return ResponseEntity.ok(ApiResponse.ok(categoryService.getCategoriesByDepartment(departmentId)));
        }
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getAllCategories()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get issue category by ID (Public)")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getCategoryById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create an issue category (Admin only)")
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(@Valid @RequestBody CategoryDto dto) {
        CategoryDto created = categoryService.createCategory(dto);
        return new ResponseEntity<>(ApiResponse.ok("Category created", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an issue category (Admin only)")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDto dto) {
        CategoryDto updated = categoryService.updateCategory(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Category updated", updated));
    }
}
