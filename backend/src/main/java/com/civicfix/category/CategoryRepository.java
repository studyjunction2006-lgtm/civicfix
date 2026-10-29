package com.civicfix.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<IssueCategory, Long> {
    Optional<IssueCategory> findByCode(String code);
    List<IssueCategory> findByDepartmentId(Long departmentId);
    boolean existsByName(String name);
    boolean existsByCode(String code);
}
