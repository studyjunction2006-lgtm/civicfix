package com.civicfix.category;

import com.civicfix.audit.AuditLogService;
import com.civicfix.common.BadRequestException;
import com.civicfix.common.ResourceNotFoundException;
import com.civicfix.department.Department;
import com.civicfix.department.DepartmentRepository;
import com.civicfix.security.SecurityUtils;
import com.civicfix.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;

    public CategoryService(CategoryRepository categoryRepository,
                           DepartmentRepository departmentRepository,
                           AuditLogService auditLogService) {
        this.categoryRepository = categoryRepository;
        this.departmentRepository = departmentRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoriesByDepartment(Long departmentId) {
        return categoryRepository.findByDepartmentId(departmentId).stream()
                .map(CategoryDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        IssueCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return CategoryDto.fromEntity(cat);
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Category with name '" + dto.getName() + "' already exists");
        }
        if (categoryRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException("Category with code '" + dto.getCode() + "' already exists");
        }

        Department department = null;
        if (dto.getDepartmentId() != null) {
            department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", dto.getDepartmentId()));
        }

        IssueCategory category = new IssueCategory(
                dto.getName(),
                dto.getCode().toUpperCase().trim(),
                dto.getDescription(),
                department,
                dto.getSlaHours() > 0 ? dto.getSlaHours() : 48
        );

        IssueCategory saved = categoryRepository.save(category);

        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        auditLogService.logAction("CREATE_CATEGORY", "Category", saved.getId().toString(), currentUser.getId(), currentUser.getUsername(), currentUser.getRole().name(), "Created issue category: " + saved.getName());

        return CategoryDto.fromEntity(saved);
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        IssueCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        cat.setName(dto.getName());
        cat.setDescription(dto.getDescription());
        cat.setSlaHours(dto.getSlaHours());

        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", dto.getDepartmentId()));
            cat.setDepartment(dept);
        }

        IssueCategory updated = categoryRepository.save(cat);

        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        auditLogService.logAction("UPDATE_CATEGORY", "Category", updated.getId().toString(), currentUser.getId(), currentUser.getUsername(), currentUser.getRole().name(), "Updated issue category: " + updated.getName());

        return CategoryDto.fromEntity(updated);
    }
}
