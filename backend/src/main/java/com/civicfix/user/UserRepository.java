package com.civicfix.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByDepartmentIdAndRole(Long departmentId, Role role);
    List<User> findByDepartmentId(Long departmentId);
    Page<User> findByRole(Role role, Pageable pageable);
    long countByRole(Role role);
}
