package com.civicfix.category;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public class CategoryDto {
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Category code is required")
    @Size(max = 50)
    private String code;

    private String description;
    private Long departmentId;
    private String departmentName;

    @Min(value = 1, message = "SLA hours must be at least 1 hour")
    private int slaHours = 48;

    private Instant createdAt;

    public CategoryDto() {}

    public static CategoryDto fromEntity(IssueCategory category) {
        if (category == null) return null;
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setCode(category.getCode());
        dto.setDescription(category.getDescription());
        dto.setSlaHours(category.getSlaHours());
        dto.setCreatedAt(category.getCreatedAt());
        if (category.getDepartment() != null) {
            dto.setDepartmentId(category.getDepartment().getId());
            dto.setDepartmentName(category.getDepartment().getName());
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public int getSlaHours() {
        return slaHours;
    }

    public void setSlaHours(int slaHours) {
        this.slaHours = slaHours;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
