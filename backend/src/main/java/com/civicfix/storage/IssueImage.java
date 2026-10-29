package com.civicfix.storage;

import com.civicfix.issue.Issue;
import com.civicfix.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "issue_images")
public class IssueImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false, length = 30)
    private IssueImageType imageType = IssueImageType.REPORT_EVIDENCE;

    @Column(length = 255)
    private String caption;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public IssueImage() {}

    public IssueImage(Issue issue, User uploader, String imageUrl, IssueImageType imageType, String caption) {
        this.issue = issue;
        this.uploader = uploader;
        this.imageUrl = imageUrl;
        this.imageType = imageType;
        this.caption = caption;
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

    public User getUploader() {
        return uploader;
    }

    public void setUploader(User uploader) {
        this.uploader = uploader;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public IssueImageType getImageType() {
        return imageType;
    }

    public void setImageType(IssueImageType imageType) {
        this.imageType = imageType;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
