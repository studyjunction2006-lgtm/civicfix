package com.civicfix.issue;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class StatusTransitionRequest {

    @NotNull(message = "New target status is required")
    private IssueStatus newStatus;

    private String notes;

    // Optional resolution proof photos when transitioning to RESOLVED
    private List<String> resolutionImageUrls;

    public StatusTransitionRequest() {}

    public StatusTransitionRequest(IssueStatus newStatus, String notes) {
        this.newStatus = newStatus;
        this.notes = notes;
    }

    public IssueStatus getNewStatus() { return newStatus; }
    public void setNewStatus(IssueStatus newStatus) { this.newStatus = newStatus; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<String> getResolutionImageUrls() { return resolutionImageUrls; }
    public void setResolutionImageUrls(List<String> resolutionImageUrls) { this.resolutionImageUrls = resolutionImageUrls; }
}
