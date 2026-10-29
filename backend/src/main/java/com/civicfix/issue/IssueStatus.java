package com.civicfix.issue;

public enum IssueStatus {
    REPORT_CREATED("Report Created"),
    UNDER_REVIEW("Under Review"),
    VERIFIED("Verified"),
    REJECTED("Rejected"),
    ASSIGNED("Assigned"),
    ACCEPTED("Accepted"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    CITIZEN_VERIFICATION("Citizen Verification"),
    REOPENED("Reopened"),
    CLOSED("Closed");

    private final String displayName;

    IssueStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
