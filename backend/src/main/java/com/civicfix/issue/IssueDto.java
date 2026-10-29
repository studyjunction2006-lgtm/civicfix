package com.civicfix.issue;

import com.civicfix.storage.IssueImage;
import com.civicfix.storage.IssueImageType;
import com.civicfix.user.UserDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class IssueDto {
    private Long id;
    private String trackingNumber;
    private String title;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Long departmentId;
    private String departmentName;
    private UserDto reporter;
    private UserDto assignedWorker;
    private IssueStatus status;
    private String statusDisplayName;
    private IssueSeverity severity;
    private IssuePriority calculatedPriority;
    private double priorityScore;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String addressText;
    private int confirmationCount;
    private int reopenCount;
    private Instant dueDate;
    private Instant resolvedAt;
    private Instant closedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private List<IssueImageDto> images;
    private List<StatusHistoryDto> timeline;

    public IssueDto() {}

    public static IssueDto fromEntity(Issue issue) {
        if (issue == null) return null;
        IssueDto dto = new IssueDto();
        dto.setId(issue.getId());
        dto.setTrackingNumber(issue.getTrackingNumber());
        dto.setTitle(issue.getTitle());
        dto.setDescription(issue.getDescription());

        if (issue.getCategory() != null) {
            dto.setCategoryId(issue.getCategory().getId());
            dto.setCategoryName(issue.getCategory().getName());
        }

        if (issue.getDepartment() != null) {
            dto.setDepartmentId(issue.getDepartment().getId());
            dto.setDepartmentName(issue.getDepartment().getName());
        }

        dto.setReporter(UserDto.fromEntity(issue.getReporter()));
        dto.setAssignedWorker(UserDto.fromEntity(issue.getAssignedWorker()));
        dto.setStatus(issue.getStatus());
        dto.setStatusDisplayName(issue.getStatus() != null ? issue.getStatus().getDisplayName() : null);
        dto.setSeverity(issue.getSeverity());
        dto.setCalculatedPriority(issue.getCalculatedPriority());
        dto.setPriorityScore(issue.getPriorityScore());
        dto.setLatitude(issue.getLatitude());
        dto.setLongitude(issue.getLongitude());
        dto.setAddressText(issue.getAddressText());
        dto.setConfirmationCount(issue.getConfirmationCount());
        dto.setReopenCount(issue.getReopenCount());
        dto.setDueDate(issue.getDueDate());
        dto.setResolvedAt(issue.getResolvedAt());
        dto.setClosedAt(issue.getClosedAt());
        dto.setCreatedAt(issue.getCreatedAt());
        dto.setUpdatedAt(issue.getUpdatedAt());

        if (issue.getImages() != null) {
            dto.setImages(issue.getImages().stream().map(IssueImageDto::fromEntity).toList());
        }

        if (issue.getStatusHistory() != null) {
            dto.setTimeline(issue.getStatusHistory().stream().map(StatusHistoryDto::fromEntity).toList());
        }

        return dto;
    }

    public static class IssueImageDto {
        private Long id;
        private String imageUrl;
        private IssueImageType imageType;
        private String caption;
        private Instant createdAt;

        public static IssueImageDto fromEntity(IssueImage img) {
            if (img == null) return null;
            IssueImageDto dto = new IssueImageDto();
            dto.id = img.getId();
            dto.imageUrl = img.getImageUrl();
            dto.imageType = img.getImageType();
            dto.caption = img.getCaption();
            dto.createdAt = img.getCreatedAt();
            return dto;
        }

        public Long getId() { return id; }
        public String getImageUrl() { return imageUrl; }
        public IssueImageType getImageType() { return imageType; }
        public String getCaption() { return caption; }
        public Instant getCreatedAt() { return createdAt; }
    }

    public static class StatusHistoryDto {
        private Long id;
        private IssueStatus previousStatus;
        private IssueStatus newStatus;
        private String newStatusDisplayName;
        private String actorName;
        private String actorRole;
        private String reasonOrNotes;
        private Instant createdAt;

        public static StatusHistoryDto fromEntity(IssueStatusHistory history) {
            if (history == null) return null;
            StatusHistoryDto dto = new StatusHistoryDto();
            dto.id = history.getId();
            dto.previousStatus = history.getPreviousStatus();
            dto.newStatus = history.getNewStatus();
            dto.newStatusDisplayName = history.getNewStatus() != null ? history.getNewStatus().getDisplayName() : null;
            if (history.getActor() != null) {
                dto.actorName = history.getActor().getFullName();
                dto.actorRole = history.getActor().getRole().name();
            }
            dto.reasonOrNotes = history.getReasonOrNotes();
            dto.createdAt = history.getCreatedAt();
            return dto;
        }

        public Long getId() { return id; }
        public IssueStatus getPreviousStatus() { return previousStatus; }
        public IssueStatus getNewStatus() { return newStatus; }
        public String getNewStatusDisplayName() { return newStatusDisplayName; }
        public String getActorName() { return actorName; }
        public String getActorRole() { return actorRole; }
        public String getReasonOrNotes() { return reasonOrNotes; }
        public Instant getCreatedAt() { return createdAt; }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public UserDto getReporter() { return reporter; }
    public void setReporter(UserDto reporter) { this.reporter = reporter; }
    public UserDto getAssignedWorker() { return assignedWorker; }
    public void setAssignedWorker(UserDto assignedWorker) { this.assignedWorker = assignedWorker; }
    public IssueStatus getStatus() { return status; }
    public void setStatus(IssueStatus status) { this.status = status; }
    public String getStatusDisplayName() { return statusDisplayName; }
    public void setStatusDisplayName(String statusDisplayName) { this.statusDisplayName = statusDisplayName; }
    public IssueSeverity getSeverity() { return severity; }
    public void setSeverity(IssueSeverity severity) { this.severity = severity; }
    public IssuePriority getCalculatedPriority() { return calculatedPriority; }
    public void setCalculatedPriority(IssuePriority calculatedPriority) { this.calculatedPriority = calculatedPriority; }
    public double getPriorityScore() { return priorityScore; }
    public void setPriorityScore(double priorityScore) { this.priorityScore = priorityScore; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public String getAddressText() { return addressText; }
    public void setAddressText(String addressText) { this.addressText = addressText; }
    public int getConfirmationCount() { return confirmationCount; }
    public void setConfirmationCount(int confirmationCount) { this.confirmationCount = confirmationCount; }
    public int getReopenCount() { return reopenCount; }
    public void setReopenCount(int reopenCount) { this.reopenCount = reopenCount; }
    public Instant getDueDate() { return dueDate; }
    public void setDueDate(Instant dueDate) { this.dueDate = dueDate; }
    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public List<IssueImageDto> getImages() { return images; }
    public void setImages(List<IssueImageDto> images) { this.images = images; }
    public List<StatusHistoryDto> getTimeline() { return timeline; }
    public void setTimeline(List<StatusHistoryDto> timeline) { this.timeline = timeline; }
}
