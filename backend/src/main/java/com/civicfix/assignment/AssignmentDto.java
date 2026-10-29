package com.civicfix.assignment;

import com.civicfix.issue.IssueDto;
import com.civicfix.user.UserDto;
import java.time.Instant;

public class AssignmentDto {
    private Long id;
    private Long issueId;
    private IssueDto issue;
    private UserDto worker;
    private UserDto assignedBy;
    private AssignmentStatus status;
    private String declineReason;
    private String workerNotes;
    private Instant assignedAt;
    private Instant acceptedAt;
    private Instant completedAt;

    public AssignmentDto() {}

    public static AssignmentDto fromEntity(WorkerAssignment assignment) {
        if (assignment == null) return null;
        AssignmentDto dto = new AssignmentDto();
        dto.setId(assignment.getId());
        dto.setIssueId(assignment.getIssue().getId());
        dto.setIssue(IssueDto.fromEntity(assignment.getIssue()));
        dto.setWorker(UserDto.fromEntity(assignment.getWorker()));
        dto.setAssignedBy(UserDto.fromEntity(assignment.getAssignedBy()));
        dto.setStatus(assignment.getStatus());
        dto.setDeclineReason(assignment.getDeclineReason());
        dto.setWorkerNotes(assignment.getWorkerNotes());
        dto.setAssignedAt(assignment.getAssignedAt());
        dto.setAcceptedAt(assignment.getAcceptedAt());
        dto.setCompletedAt(assignment.getCompletedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIssueId() { return issueId; }
    public void setIssueId(Long issueId) { this.issueId = issueId; }
    public IssueDto getIssue() { return issue; }
    public void setIssue(IssueDto issue) { this.issue = issue; }
    public UserDto getWorker() { return worker; }
    public void setWorker(UserDto worker) { this.worker = worker; }
    public UserDto getAssignedBy() { return assignedBy; }
    public void setAssignedBy(UserDto assignedBy) { this.assignedBy = assignedBy; }
    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }
    public String getDeclineReason() { return declineReason; }
    public void setDeclineReason(String declineReason) { this.declineReason = declineReason; }
    public String getWorkerNotes() { return workerNotes; }
    public void setWorkerNotes(String workerNotes) { this.workerNotes = workerNotes; }
    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }
    public Instant getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(Instant acceptedAt) { this.acceptedAt = acceptedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
