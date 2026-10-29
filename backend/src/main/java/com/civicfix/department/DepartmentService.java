package com.civicfix.department;

import com.civicfix.audit.AuditLogService;
import com.civicfix.common.BadRequestException;
import com.civicfix.common.ResourceNotFoundException;
import com.civicfix.security.SecurityUtils;
import com.civicfix.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;

    public DepartmentService(DepartmentRepository departmentRepository, AuditLogService auditLogService) {
        this.departmentRepository = departmentRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(DepartmentDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentDto getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return DepartmentDto.fromEntity(dept);
    }

    @Transactional
    public DepartmentDto createDepartment(DepartmentDto dto) {
        if (departmentRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Department with name '" + dto.getName() + "' already exists");
        }
        if (departmentRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException("Department with code '" + dto.getCode() + "' already exists");
        }

        Department dept = new Department(
                dto.getName(),
                dto.getCode().toUpperCase().trim(),
                dto.getDescription(),
                dto.getContactEmail(),
                dto.getContactPhone()
        );

        Department saved = departmentRepository.save(dept);

        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        auditLogService.logAction("CREATE_DEPARTMENT", "Department", saved.getId().toString(), currentUser.getId(), currentUser.getUsername(), currentUser.getRole().name(), "Created department: " + saved.getName());

        return DepartmentDto.fromEntity(saved);
    }

    @Transactional
    public DepartmentDto updateDepartment(Long id, DepartmentDto dto) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        dept.setName(dto.getName());
        dept.setDescription(dto.getDescription());
        dept.setContactEmail(dto.getContactEmail());
        dept.setContactPhone(dto.getContactPhone());

        Department updated = departmentRepository.save(dept);

        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        auditLogService.logAction("UPDATE_DEPARTMENT", "Department", updated.getId().toString(), currentUser.getId(), currentUser.getUsername(), currentUser.getRole().name(), "Updated department: " + updated.getName());

        return DepartmentDto.fromEntity(updated);
    }
}
