package com.civicfix.issue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class IssuePriorityCalculator {

    private final double severityWeight;
    private final double confirmationWeight;
    private final double ageHourlyWeight;
    private final double overduePenalty;
    private final double reopenPenalty;

    public IssuePriorityCalculator(
            @Value("${civicfix.priority-weights.severity-weight:25.0}") double severityWeight,
            @Value("${civicfix.priority-weights.confirmation-weight:5.0}") double confirmationWeight,
            @Value("${civicfix.priority-weights.age-hourly-weight:0.5}") double ageHourlyWeight,
            @Value("${civicfix.priority-weights.overdue-penalty:20.0}") double overduePenalty,
            @Value("${civicfix.priority-weights.reopen-penalty:15.0}") double reopenPenalty) {
        this.severityWeight = severityWeight;
        this.confirmationWeight = confirmationWeight;
        this.ageHourlyWeight = ageHourlyWeight;
        this.overduePenalty = overduePenalty;
        this.reopenPenalty = reopenPenalty;
    }

    public record PriorityCalculationResult(double score, IssuePriority priority) {}

    public PriorityCalculationResult calculate(IssueSeverity severity,
                                                int confirmationCount,
                                                Instant createdAt,
                                                Instant dueDate,
                                                int reopenCount,
                                                IssueStatus status) {

        double baseScore = severity != null ? severity.getBasePoints() : IssueSeverity.LOW.getBasePoints();

        // 1. Community validation impact (up to 30 bonus points)
        double confirmationScore = Math.min(Math.max(0, confirmationCount) * confirmationWeight, 30.0);

        // 2. Age of report impact (0.5 points per hour, up to 20 points)
        long ageHours = 0;
        if (createdAt != null) {
            ageHours = Math.max(0, Duration.between(createdAt, Instant.now()).toHours());
        }
        double ageScore = Math.min(ageHours * ageHourlyWeight, 20.0);

        // 3. Overdue SLA penalty (if past due date and not yet resolved/closed)
        double overdueScore = 0.0;
        if (dueDate != null && Instant.now().isAfter(dueDate)) {
            if (status != IssueStatus.RESOLVED && status != IssueStatus.CLOSED && status != IssueStatus.REJECTED) {
                overdueScore = overduePenalty;
            }
        }

        // 4. Repeated failure / reopening penalty (15 pts per reopening, up to 30 pts)
        double reopenScore = Math.min(Math.max(0, reopenCount) * reopenPenalty, 30.0);

        double totalScore = Math.round((baseScore + confirmationScore + ageScore + overdueScore + reopenScore) * 10.0) / 10.0;

        // Map deterministic score into priority tier
        IssuePriority priority;
        if (totalScore < 30.0) {
            priority = IssuePriority.LOW;
        } else if (totalScore < 55.0) {
            priority = IssuePriority.MEDIUM;
        } else if (totalScore < 80.0) {
            priority = IssuePriority.HIGH;
        } else {
            priority = IssuePriority.CRITICAL;
        }

        return new PriorityCalculationResult(totalScore, priority);
    }
}
