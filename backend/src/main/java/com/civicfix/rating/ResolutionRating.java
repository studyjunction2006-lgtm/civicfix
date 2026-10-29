package com.civicfix.rating;

import com.civicfix.issue.Issue;
import com.civicfix.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;

@Entity
@Table(name = "resolution_ratings", uniqueConstraints = {
        @UniqueConstraint(name = "uq_rating_issue_citizen", columnNames = {"issue_id", "citizen_id"})
})
public class ResolutionRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "citizen_id", nullable = false)
    private User citizen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "worker_id")
    private User worker;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int rating;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ResolutionRating() {}

    public ResolutionRating(Issue issue, User citizen, User worker, int rating, String feedback) {
        this.issue = issue;
        this.citizen = citizen;
        this.worker = worker;
        this.rating = rating;
        this.feedback = feedback;
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

    public User getWorker() {
        return worker;
    }

    public void setWorker(User worker) {
        this.worker = worker;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
