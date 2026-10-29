package com.civicfix.issue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class IssuePriorityCalculatorTest {

    private IssuePriorityCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new IssuePriorityCalculator(25.0, 5.0, 0.5, 20.0, 15.0);
    }

    @Test
    @DisplayName("Low severity fresh issue without confirmations produces LOW priority")
    void testLowSeverityFreshIssue() {
        var result = calculator.calculate(
                IssueSeverity.LOW,
                0,
                Instant.now(),
                Instant.now().plus(48, ChronoUnit.HOURS),
                0,
                IssueStatus.REPORT_CREATED
        );

        assertEquals(10.0, result.score());
        assertEquals(IssuePriority.LOW, result.priority());
    }

    @Test
    @DisplayName("Medium severity issue with 6 confirmations escalates to HIGH priority")
    void testConfirmationsEscalatePriority() {
        var result = calculator.calculate(
                IssueSeverity.MEDIUM,
                6, // 6 * 5 = 30 pts (max confirmation bonus)
                Instant.now(),
                Instant.now().plus(24, ChronoUnit.HOURS),
                0,
                IssueStatus.REPORT_CREATED
        );

        // Base 25 + 30 confirmations = 55 -> HIGH priority (>= 55.0)
        assertTrue(result.score() >= 55.0);
        assertEquals(IssuePriority.HIGH, result.priority());
    }

    @Test
    @DisplayName("Critical severity with overdue penalty escalates to CRITICAL priority")
    void testOverduePenaltyEscalation() {
        var result = calculator.calculate(
                IssueSeverity.CRITICAL, // 70 pts
                2, // 10 pts
                Instant.now().minus(72, ChronoUnit.HOURS), // 20 pts max age
                Instant.now().minus(24, ChronoUnit.HOURS), // Overdue! 20 pts
                0,
                IssueStatus.IN_PROGRESS
        );

        // 70 + 10 + 20 + 20 = 120 -> CRITICAL priority (>= 80.0)
        assertTrue(result.score() >= 80.0);
        assertEquals(IssuePriority.CRITICAL, result.priority());
    }

    @Test
    @DisplayName("Reopened issue receives reopening penalty points")
    void testReopenPenaltyApplied() {
        var freshResult = calculator.calculate(
                IssueSeverity.MEDIUM,
                0,
                Instant.now(),
                Instant.now().plus(24, ChronoUnit.HOURS),
                0,
                IssueStatus.REPORT_CREATED
        );

        var reopenedResult = calculator.calculate(
                IssueSeverity.MEDIUM,
                0,
                Instant.now(),
                Instant.now().plus(24, ChronoUnit.HOURS),
                1, // 1 reopening = +15 pts
                IssueStatus.REOPENED
        );

        assertEquals(freshResult.score() + 15.0, reopenedResult.score());
    }
}
