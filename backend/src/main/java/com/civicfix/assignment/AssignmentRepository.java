package com.civicfix.assignment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<WorkerAssignment, Long> {
    List<WorkerAssignment> findByIssueIdOrderByAssignedAtDesc(Long issueId);
    Optional<WorkerAssignment> findFirstByIssueIdAndStatus(Long issueId, AssignmentStatus status);
    Page<WorkerAssignment> findByWorkerIdOrderByAssignedAtDesc(Long workerId, Pageable pageable);
    long countByWorkerIdAndStatus(Long workerId, AssignmentStatus status);
}
