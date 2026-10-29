package com.civicfix.issue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long>, JpaSpecificationExecutor<Issue> {

    Optional<Issue> findByTrackingNumber(String trackingNumber);

    boolean existsByTrackingNumber(String trackingNumber);

    Page<Issue> findByReporterIdOrderByCreatedAtDesc(Long reporterId, Pageable pageable);

    Page<Issue> findByAssignedWorkerIdOrderByCreatedAtDesc(Long workerId, Pageable pageable);

    Page<Issue> findByDepartmentIdOrderByCreatedAtDesc(Long departmentId, Pageable pageable);

    List<Issue> findTop50ByOrderByCreatedAtDesc();

    long countByStatus(IssueStatus status);

    long countByCalculatedPriority(IssuePriority priority);

    long countByDepartmentId(Long departmentId);

    @Query("SELECT COUNT(i) FROM Issue i WHERE i.dueDate IS NOT NULL AND i.dueDate < CURRENT_TIMESTAMP AND i.status NOT IN ('RESOLVED', 'CLOSED', 'REJECTED')")
    long countOverdueIssues();
}
