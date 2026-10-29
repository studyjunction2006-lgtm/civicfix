package com.civicfix.confirmation;

import com.civicfix.issue.Issue;
import com.civicfix.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "community_confirmations", uniqueConstraints = {
        @UniqueConstraint(name = "uq_issue_citizen", columnNames = {"issue_id", "citizen_id"})
})
public class CommunityConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "citizen_id", nullable = false)
    private User citizen;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public CommunityConfirmation() {}

    public CommunityConfirmation(Issue issue, User citizen, String note) {
        this.issue = issue;
        this.citizen = citizen;
        this.note = note;
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

    public User getCitizen() {
        return citizen;
    }

    public void setCitizen(User citizen) {
        this.citizen = citizen;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
