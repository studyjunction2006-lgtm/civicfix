package com.civicfix.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public class DepartmentDto {
    private Long id;

    @NotBlank(message = "Department name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Department code is required")
    @Size(max = 50)
    private String code;

    private String description;
    private String contactEmail;
    private String contactPhone;
    private Instant createdAt;

    public DepartmentDto() {}

    public static DepartmentDto fromEntity(Department dept) {
        if (dept == null) return null;
        DepartmentDto dto = new DepartmentDto();
        dto.setId(dept.getId());
        dto.setName(dept.getName());
        dto.setCode(dept.getCode());
        dto.setDescription(dept.getDescription());
        dto.setContactEmail(dept.getContactEmail());
        dto.setContactPhone(dept.getContactPhone());
        dto.setCreatedAt(dept.getCreatedAt());
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

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
