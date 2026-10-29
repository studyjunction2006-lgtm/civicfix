package com.civicfix.assignment;

import com.civicfix.issue.Issue;
import com.civicfix.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "worker_assignments")
public class WorkerAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AssignmentStatus status = AssignmentStatus.PENDING;

    @Column(name = "decline_reason", columnDefinition = "TEXT")
    private String declineReason;

    @Column(name = "worker_notes", columnDefinition = "TEXT")
    private String workerNotes;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt = Instant.now();

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public WorkerAssignment() {}

    public WorkerAssignment(Issue issue, User worker, User assignedBy) {
        this.issue = issue;
        this.worker = worker;
        this.assignedBy = assignedBy;
        this.status = AssignmentStatus.PENDING;
        this.assignedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public User getWorker() {
        return worker;
    }

    public void setWorker(User worker) {
        this.worker = worker;
    }

    public User getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(User assignedBy) {
        this.assignedBy = assignedBy;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }

    public String getDeclineReason() {
        return declineReason;
    }

    public void setDeclineReason(String declineReason) {
        this.declineReason = declineReason;
    }

    public String getWorkerNotes() {
        return workerNotes;
    }

    public void setWorkerNotes(String workerNotes) {
        this.workerNotes = workerNotes;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
