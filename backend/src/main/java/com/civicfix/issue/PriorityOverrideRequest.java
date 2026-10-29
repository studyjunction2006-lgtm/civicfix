package com.civicfix.issue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PriorityOverrideRequest {

    @NotNull(message = "Priority is required")
    private IssuePriority priority;

    @NotBlank(message = "An administrative reason for overriding the calculated priority is required")
    private String reason;

    public PriorityOverrideRequest() {}

    public PriorityOverrideRequest(IssuePriority priority, String reason) {
        this.priority = priority;
        this.reason = reason;
    }

    public IssuePriority getPriority() { return priority; }
    public void setPriority(IssuePriority priority) { this.priority = priority; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
