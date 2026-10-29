package com.civicfix.assignment;

import jakarta.validation.constraints.NotNull;

public class AssignmentActionRequest {

    public enum Action {
        ACCEPT,
        DECLINE
    }

    @NotNull(message = "Action is required (ACCEPT or DECLINE)")
    private Action action;

    private String declineReason;
    private String workerNotes;

    public AssignmentActionRequest() {}

    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }
    public String getDeclineReason() { return declineReason; }
    public void setDeclineReason(String declineReason) { this.declineReason = declineReason; }
    public String getWorkerNotes() { return workerNotes; }
    public void setWorkerNotes(String workerNotes) { this.workerNotes = workerNotes; }
}
