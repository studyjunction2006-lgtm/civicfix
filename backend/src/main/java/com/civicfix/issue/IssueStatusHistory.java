package com.civicfix.issue;

import com.civicfix.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "issue_status_history")
public class IssueStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 50)
    private IssueStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 50)
    private IssueStatus newStatus;

    @Column(name = "reason_or_notes", columnDefinition = "TEXT")
    private String reasonOrNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public IssueStatusHistory() {}

    public IssueStatusHistory(Issue issue, User actor, IssueStatus previousStatus, IssueStatus newStatus, String reasonOrNotes) {
        this.issue = issue;
        this.actor = actor;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reasonOrNotes = reasonOrNotes;
        this.createdAt = Instant.now();
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

    public User getActor() {
        return actor;
    }

    public void setActor(User actor) {
        this.actor = actor;
    }

    public IssueStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(IssueStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public IssueStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(IssueStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getReasonOrNotes() {
        return reasonOrNotes;
    }

    public void setReasonOrNotes(String reasonOrNotes) {
        this.reasonOrNotes = reasonOrNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
