package com.civicfix.analytics;

import com.civicfix.category.CategoryRepository;
import com.civicfix.category.IssueCategory;
import com.civicfix.department.Department;
import com.civicfix.department.DepartmentRepository;
import com.civicfix.issue.Issue;
import com.civicfix.issue.IssuePriority;
import com.civicfix.issue.IssueRepository;
import com.civicfix.issue.IssueSeverity;
import com.civicfix.issue.IssueStatus;
import com.civicfix.rating.RatingRepository;
import com.civicfix.user.Role;
import com.civicfix.user.User;
import com.civicfix.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final IssueRepository issueRepository;
    private final DepartmentRepository departmentRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;

    public AnalyticsService(IssueRepository issueRepository,
                            DepartmentRepository departmentRepository,
                            CategoryRepository categoryRepository,
                            UserRepository userRepository,
                            RatingRepository ratingRepository) {
        this.issueRepository = issueRepository;
        this.departmentRepository = departmentRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryDto getPlatformAnalytics() {
        List<Issue> allIssues = issueRepository.findAll();
        List<Department> allDepts = departmentRepository.findAll();
        List<IssueCategory> allCategories = categoryRepository.findAll();
        List<User> workers = userRepository.findByRole(Role.WORKER);

        AnalyticsSummaryDto dto = new AnalyticsSummaryDto();

        long total = allIssues.size();
        long open = allIssues.stream().filter(i -> i.getStatus() == IssueStatus.REPORT_CREATED || i.getStatus() == IssueStatus.UNDER_REVIEW || i.getStatus() == IssueStatus.VERIFIED).count();
        long inProgress = allIssues.stream().filter(i -> i.getStatus() == IssueStatus.ASSIGNED || i.getStatus() == IssueStatus.ACCEPTED || i.getStatus() == IssueStatus.IN_PROGRESS).count();
        long resolved = allIssues.stream().filter(i -> i.getStatus() == IssueStatus.RESOLVED || i.getStatus() == IssueStatus.CITIZEN_VERIFICATION).count();
        long closed = allIssues.stream().filter(i -> i.getStatus() == IssueStatus.CLOSED).count();
        long overdue = allIssues.stream().filter(i -> i.getDueDate() != null && Instant.now().isAfter(i.getDueDate()) && i.getStatus() != IssueStatus.RESOLVED && i.getStatus() != IssueStatus.CLOSED && i.getStatus() != IssueStatus.REJECTED).count();

        double resolutionRate = total > 0 ? Math.round(((double) (resolved + closed) / total * 100.0) * 10.0) / 10.0 : 0.0;

        // Calculate average resolution time in hours
        List<Issue> completedIssues = allIssues.stream()
                .filter(i -> i.getResolvedAt() != null && i.getCreatedAt() != null)
                .toList();

        double avgHours = 0.0;
        if (!completedIssues.isEmpty()) {
            double totalHours = completedIssues.stream()
                    .mapToDouble(i -> Math.max(1.0, Duration.between(i.getCreatedAt(), i.getResolvedAt()).toHours()))
                    .sum();
            avgHours = Math.round((totalHours / completedIssues.size()) * 10.0) / 10.0;
        }

        dto.setTotalIssues(total);
        dto.setOpenIssues(open);
        dto.setInProgressIssues(inProgress);
        dto.setResolvedIssues(resolved);
        dto.setClosedIssues(closed);
        dto.setOverdueIssues(overdue);
        dto.setResolutionRate(resolutionRate);
        dto.setAverageResolutionHours(avgHours);

        // Issues by Category
        Map<String, Long> categoryCount = allIssues.stream()
                .filter(i -> i.getCategory() != null)
                .collect(Collectors.groupingBy(i -> i.getCategory().getName(), Collectors.counting()));

        List<AnalyticsSummaryDto.CategoryStat> catStats = allCategories.stream()
                .map(cat -> new AnalyticsSummaryDto.CategoryStat(cat.getName(), cat.getCode(), categoryCount.getOrDefault(cat.getName(), 0L)))
                .sorted(Comparator.comparingLong(AnalyticsSummaryDto.CategoryStat::getCount).reversed())
                .toList();
        dto.setIssuesByCategory(catStats);

        // Issues by Status
        Map<IssueStatus, Long> statusCount = allIssues.stream()
                .collect(Collectors.groupingBy(Issue::getStatus, Collectors.counting()));

        List<AnalyticsSummaryDto.StatusStat> statusStats = Arrays.stream(IssueStatus.values())
                .map(st -> new AnalyticsSummaryDto.StatusStat(st.name(), st.getDisplayName(), statusCount.getOrDefault(st, 0L)))
                .filter(st -> st.getCount() > 0)
                .toList();
        dto.setIssuesByStatus(statusStats);

        // Issues by Severity
        Map<IssueSeverity, Long> severityCount = allIssues.stream()
                .collect(Collectors.groupingBy(Issue::getSeverity, Collectors.counting()));

        List<AnalyticsSummaryDto.SeverityStat> severityStats = Arrays.stream(IssueSeverity.values())
                .map(sev -> new AnalyticsSummaryDto.SeverityStat(sev.name(), severityCount.getOrDefault(sev, 0L)))
                .toList();
        dto.setIssuesBySeverity(severityStats);

        // Issues by Priority
        Map<IssuePriority, Long> priorityCount = allIssues.stream()
                .collect(Collectors.groupingBy(Issue::getCalculatedPriority, Collectors.counting()));

        List<AnalyticsSummaryDto.PriorityStat> priorityStats = Arrays.stream(IssuePriority.values())
                .map(pr -> new AnalyticsSummaryDto.PriorityStat(pr.name(), priorityCount.getOrDefault(pr, 0L)))
                .toList();
        dto.setIssuesByPriority(priorityStats);

        // Department Performance
        List<AnalyticsSummaryDto.DepartmentStat> deptStats = allDepts.stream().map(dept -> {
            long deptTotal = allIssues.stream().filter(i -> i.getDepartment() != null && i.getDepartment().getId().equals(dept.getId())).count();
            long deptResolved = allIssues.stream().filter(i -> i.getDepartment() != null && i.getDepartment().getId().equals(dept.getId()) && (i.getStatus() == IssueStatus.RESOLVED || i.getStatus() == IssueStatus.CLOSED)).count();
            double deptRate = deptTotal > 0 ? Math.round(((double) deptResolved / deptTotal * 100.0) * 10.0) / 10.0 : 0.0;
            return new AnalyticsSummaryDto.DepartmentStat(dept.getId(), dept.getName(), deptTotal, deptResolved, deptRate);
        }).toList();
        dto.setDepartmentPerformance(deptStats);

        // Monthly trends (group by Month-Year)
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy").withZone(ZoneId.systemDefault());
        Map<String, long[]> monthlyData = new LinkedHashMap<>();

        for (Issue issue : allIssues) {
            String month = monthFormatter.format(issue.getCreatedAt());
            monthlyData.computeIfAbsent(month, k -> new long[2])[0]++; // reported

            if (issue.getResolvedAt() != null) {
                String resolvedMonth = monthFormatter.format(issue.getResolvedAt());
                monthlyData.computeIfAbsent(resolvedMonth, k -> new long[2])[1]++; // resolved
            }
        }

        List<AnalyticsSummaryDto.MonthlyTrendStat> trends = monthlyData.entrySet().stream()
                .map(e -> new AnalyticsSummaryDto.MonthlyTrendStat(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .toList();
        dto.setMonthlyTrends(trends);

        // Worker Workload
        List<AnalyticsSummaryDto.WorkerWorkloadStat> workerStats = workers.stream().map(w -> {
            long active = allIssues.stream().filter(i -> i.getAssignedWorker() != null && i.getAssignedWorker().getId().equals(w.getId()) && (i.getStatus() == IssueStatus.ASSIGNED || i.getStatus() == IssueStatus.ACCEPTED || i.getStatus() == IssueStatus.IN_PROGRESS)).count();
            long done = allIssues.stream().filter(i -> i.getAssignedWorker() != null && i.getAssignedWorker().getId().equals(w.getId()) && (i.getStatus() == IssueStatus.RESOLVED || i.getStatus() == IssueStatus.CLOSED)).count();
            Double avgRating = ratingRepository.getAverageRatingForWorker(w.getId());
            double ratingVal = avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 5.0;
            String deptName = w.getDepartment() != null ? w.getDepartment().getName() : "General";
            return new AnalyticsSummaryDto.WorkerWorkloadStat(w.getId(), w.getFullName(), deptName, active, done, ratingVal);
        }).toList();
        dto.setWorkerWorkloads(workerStats);

        return dto;
    }
}
