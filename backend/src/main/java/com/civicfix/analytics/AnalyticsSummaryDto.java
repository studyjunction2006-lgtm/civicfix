package com.civicfix.analytics;

import java.util.List;
import java.util.Map;

public class AnalyticsSummaryDto {
    private long totalIssues;
    private long openIssues;
    private long inProgressIssues;
    private long resolvedIssues;
    private long closedIssues;
    private long overdueIssues;
    private double resolutionRate;
    private double averageResolutionHours;

    private List<CategoryStat> issuesByCategory;
    private List<StatusStat> issuesByStatus;
    private List<SeverityStat> issuesBySeverity;
    private List<PriorityStat> issuesByPriority;
    private List<DepartmentStat> departmentPerformance;
    private List<MonthlyTrendStat> monthlyTrends;
    private List<WorkerWorkloadStat> workerWorkloads;

    public AnalyticsSummaryDto() {}

    public static class CategoryStat {
        private String category;
        private String code;
        private long count;

        public CategoryStat(String category, String code, long count) {
            this.category = category;
            this.code = code;
            this.count = count;
        }

        public String getCategory() { return category; }
        public String getCode() { return code; }
        public long getCount() { return count; }
    }

    public static class StatusStat {
        private String status;
        private String displayName;
        private long count;

        public StatusStat(String status, String displayName, long count) {
            this.status = status;
            this.displayName = displayName;
            this.count = count;
        }

        public String getStatus() { return status; }
        public String getDisplayName() { return displayName; }
        public long getCount() { return count; }
    }

    public static class SeverityStat {
        private String severity;
        private long count;

        public SeverityStat(String severity, long count) {
            this.severity = severity;
            this.count = count;
        }

        public String getSeverity() { return severity; }
        public long getCount() { return count; }
    }

    public static class PriorityStat {
        private String priority;
        private long count;

        public PriorityStat(String priority, long count) {
            this.priority = priority;
            this.count = count;
        }

        public String getPriority() { return priority; }
        public long getCount() { return count; }
    }

    public static class DepartmentStat {
        private Long departmentId;
        private String departmentName;
        private long totalIssues;
        private long resolvedIssues;
        private double resolutionRate;

        public DepartmentStat(Long departmentId, String departmentName, long totalIssues, long resolvedIssues, double resolutionRate) {
            this.departmentId = departmentId;
            this.departmentName = departmentName;
            this.totalIssues = totalIssues;
            this.resolvedIssues = resolvedIssues;
            this.resolutionRate = resolutionRate;
        }

        public Long getDepartmentId() { return departmentId; }
        public String getDepartmentName() { return departmentName; }
        public long getTotalIssues() { return totalIssues; }
        public long getResolvedIssues() { return resolvedIssues; }
        public double getResolutionRate() { return resolutionRate; }
    }

    public static class MonthlyTrendStat {
        private String month;
        private long reported;
        private long resolved;

        public MonthlyTrendStat(String month, long reported, long resolved) {
            this.month = month;
            this.reported = reported;
            this.resolved = resolved;
        }

        public String getMonth() { return month; }
        public long getReported() { return reported; }
        public long getResolved() { return resolved; }
    }

    public static class WorkerWorkloadStat {
        private Long workerId;
        private String workerName;
        private String department;
        private long activeTasks;
        private long completedTasks;
        private double averageRating;

        public WorkerWorkloadStat(Long workerId, String workerName, String department, long activeTasks, long completedTasks, double averageRating) {
            this.workerId = workerId;
            this.workerName = workerName;
            this.department = department;
            this.activeTasks = activeTasks;
            this.completedTasks = completedTasks;
            this.averageRating = averageRating;
        }

        public Long getWorkerId() { return workerId; }
        public String getWorkerName() { return workerName; }
        public String getDepartment() { return department; }
        public long getActiveTasks() { return activeTasks; }
        public long getCompletedTasks() { return completedTasks; }
        public double getAverageRating() { return averageRating; }
    }

    // Getters and Setters
    public long getTotalIssues() { return totalIssues; }
    public void setTotalIssues(long totalIssues) { this.totalIssues = totalIssues; }
    public long getOpenIssues() { return openIssues; }
    public void setOpenIssues(long openIssues) { this.openIssues = openIssues; }
    public long getInProgressIssues() { return inProgressIssues; }
    public void setInProgressIssues(long inProgressIssues) { this.inProgressIssues = inProgressIssues; }
    public long getResolvedIssues() { return resolvedIssues; }
    public void setResolvedIssues(long resolvedIssues) { this.resolvedIssues = resolvedIssues; }
    public long getClosedIssues() { return closedIssues; }
    public void setClosedIssues(long closedIssues) { this.closedIssues = closedIssues; }
    public long getOverdueIssues() { return overdueIssues; }
    public void setOverdueIssues(long overdueIssues) { this.overdueIssues = overdueIssues; }
    public double getResolutionRate() { return resolutionRate; }
    public void setResolutionRate(double resolutionRate) { this.resolutionRate = resolutionRate; }
    public double getAverageResolutionHours() { return averageResolutionHours; }
    public void setAverageResolutionHours(double averageResolutionHours) { this.averageResolutionHours = averageResolutionHours; }
    public List<CategoryStat> getIssuesByCategory() { return issuesByCategory; }
    public void setIssuesByCategory(List<CategoryStat> issuesByCategory) { this.issuesByCategory = issuesByCategory; }
    public List<StatusStat> getIssuesByStatus() { return issuesByStatus; }
    public void setIssuesByStatus(List<StatusStat> issuesByStatus) { this.issuesByStatus = issuesByStatus; }
    public List<SeverityStat> getIssuesBySeverity() { return issuesBySeverity; }
    public void setIssuesBySeverity(List<SeverityStat> issuesBySeverity) { this.issuesBySeverity = issuesBySeverity; }
    public List<PriorityStat> getIssuesByPriority() { return issuesByPriority; }
    public void setIssuesByPriority(List<PriorityStat> issuesByPriority) { this.issuesByPriority = issuesByPriority; }
    public List<DepartmentStat> getDepartmentPerformance() { return departmentPerformance; }
    public void setDepartmentPerformance(List<DepartmentStat> departmentPerformance) { this.departmentPerformance = departmentPerformance; }
    public List<MonthlyTrendStat> getMonthlyTrends() { return monthlyTrends; }
    public void setMonthlyTrends(List<MonthlyTrendStat> monthlyTrends) { this.monthlyTrends = monthlyTrends; }
    public List<WorkerWorkloadStat> getWorkerWorkloads() { return workerWorkloads; }
    public void setWorkerWorkloads(List<WorkerWorkloadStat> workerWorkloads) { this.workerWorkloads = workerWorkloads; }
}
