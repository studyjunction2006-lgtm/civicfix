package com.civicfix.issue;

import com.civicfix.category.IssueCategory;
import com.civicfix.department.Department;
import com.civicfix.storage.IssueImage;
import com.civicfix.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "issues")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 50)
    private String trackingNumber;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private IssueCategory category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_worker_id")
    private User assignedWorker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private IssueStatus status = IssueStatus.REPORT_CREATED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IssueSeverity severity = IssueSeverity.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculated_priority", nullable = false, length = 30)
    private IssuePriority calculatedPriority = IssuePriority.MEDIUM;

    @Column(name = "priority_score", nullable = false)
    private double priorityScore = 0.0;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "address_text", length = 255)
    private String addressText;

    @Column(name = "confirmation_count", nullable = false)
    private int confirmationCount = 0;

    @Column(name = "reopen_count", nullable = false)
    private int reopenCount = 0;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<IssueImage> images = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<IssueStatusHistory> statusHistory = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Issue() {}

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IssueCategory getCategory() {
        return category;
    }

    public void setCategory(IssueCategory category) {
        this.category = category;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public User getAssignedWorker() {
        return assignedWorker;
    }

    public void setAssignedWorker(User assignedWorker) {
        this.assignedWorker = assignedWorker;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public IssueSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IssueSeverity severity) {
        this.severity = severity;
    }

    public IssuePriority getCalculatedPriority() {
        return calculatedPriority;
    }

    public void setCalculatedPriority(IssuePriority calculatedPriority) {
        this.calculatedPriority = calculatedPriority;
    }

    public double getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(double priorityScore) {
        this.priorityScore = priorityScore;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getAddressText() {
        return addressText;
    }

    public void setAddressText(String addressText) {
        this.addressText = addressText;
    }

    public int getConfirmationCount() {
        return confirmationCount;
    }

    public void setConfirmationCount(int confirmationCount) {
        this.confirmationCount = confirmationCount;
    }

    public int getReopenCount() {
        return reopenCount;
    }

    public void setReopenCount(int reopenCount) {
        this.reopenCount = reopenCount;
    }

    public Instant getDueDate() {
        return dueDate;
    }

    public void setDueDate(Instant dueDate) {
        this.dueDate = dueDate;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public List<IssueImage> getImages() {
        return images;
    }

    public void setImages(List<IssueImage> images) {
        this.images = images;
    }

    public List<IssueStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<IssueStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
