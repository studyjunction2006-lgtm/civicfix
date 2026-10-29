package com.civicfix.confirmation;

import com.civicfix.audit.AuditLogService;
import com.civicfix.common.ConflictException;
import com.civicfix.issue.*;
import com.civicfix.notification.NotificationService;
import com.civicfix.user.Role;
import com.civicfix.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmationServiceTest {

    @Mock private ConfirmationRepository confirmationRepository;
    @Mock private IssueRepository issueRepository;
    @Mock private NotificationService notificationService;
    @Mock private AuditLogService auditLogService;

    private IssuePriorityCalculator priorityCalculator;
    private ConfirmationService confirmationService;

    private User reporter;
    private User validator;
    private Issue issue;

    @BeforeEach
    void setUp() {
        priorityCalculator = new IssuePriorityCalculator(25.0, 5.0, 0.5, 20.0, 15.0);
        confirmationService = new ConfirmationService(
                confirmationRepository, issueRepository, priorityCalculator, notificationService, auditLogService
        );

        reporter = new User("reporter@test.com", "hash", "Reporter", null, Role.CITIZEN, null);
        reporter.setId(1L);

        validator = new User("validator@test.com", "hash", "Validator Citizen", null, Role.CITIZEN, null);
        validator.setId(2L);

        issue = new Issue();
        issue.setId(10L);
        issue.setTrackingNumber("CF-2026-TEST");
        issue.setTitle("Broken bench");
        issue.setReporter(reporter);
        issue.setStatus(IssueStatus.REPORT_CREATED);
        issue.setSeverity(IssueSeverity.LOW);
        issue.setConfirmationCount(0);
        issue.setLatitude(BigDecimal.valueOf(30.76));
        issue.setLongitude(BigDecimal.valueOf(76.57));
    }

    @Test
    @DisplayName("Citizen can confirm an issue and increment confirmation count")
    void testConfirmIssueSuccess() {
        when(issueRepository.findById(10L)).thenReturn(Optional.of(issue));
        when(confirmationRepository.existsByIssueIdAndCitizenId(10L, 2L)).thenReturn(false);
        when(confirmationRepository.save(any(CommunityConfirmation.class))).thenAnswer(i -> {
            CommunityConfirmation c = i.getArgument(0);
            c.setId(100L);
            return c;
        });

        ConfirmationDto result = confirmationService.confirmIssue(10L, "Saw this today too", validator);

        assertNotNull(result);
        assertEquals(1, issue.getConfirmationCount());
        verify(issueRepository, times(1)).save(issue);
        verify(confirmationRepository, times(1)).save(any(CommunityConfirmation.class));
    }

    @Test
    @DisplayName("Duplicate confirmation by the same citizen is rejected with ConflictException")
    void testDuplicateConfirmationRejected() {
        when(issueRepository.findById(10L)).thenReturn(Optional.of(issue));
        when(confirmationRepository.existsByIssueIdAndCitizenId(10L, 2L)).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                confirmationService.confirmIssue(10L, "Trying again", validator)
        );

        verify(confirmationRepository, never()).save(any());
    }
}
