package com.civicfix.assignment;

import jakarta.validation.constraints.NotNull;

public class AssignWorkerRequest {

    @NotNull(message = "Worker ID is required")
    private Long workerId;

    private Long departmentId;

    private String notes;

    public AssignWorkerRequest() {}

    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
