package com.civicfix.issue;

import com.civicfix.audit.AuditLogService;
import com.civicfix.common.BadRequestException;
import com.civicfix.common.UnauthorizedException;
import com.civicfix.notification.NotificationService;
import com.civicfix.storage.IssueImageRepository;
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
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueLifecycleServiceTest {

    @Mock private IssueRepository issueRepository;
    @Mock private IssueStatusHistoryRepository statusHistoryRepository;
    @Mock private IssueImageRepository issueImageRepository;
    @Mock private NotificationService notificationService;
    @Mock private AuditLogService auditLogService;

    private IssuePriorityCalculator priorityCalculator;
    private IssueLifecycleService lifecycleService;

    private User citizen1;
    private User citizen2;
    private User authority;
    private User worker;
    private Issue testIssue;

    @BeforeEach
    void setUp() {
        priorityCalculator = new IssuePriorityCalculator(25.0, 5.0, 0.5, 20.0, 15.0);
        lifecycleService = new IssueLifecycleService(
                issueRepository,
                statusHistoryRepository,
                issueImageRepository,
                priorityCalculator,
                notificationService,
                auditLogService
        );

        citizen1 = new User("c1@test.com", "hash", "Citizen One", null, Role.CITIZEN, null);
        citizen1.setId(10L);

        citizen2 = new User("c2@test.com", "hash", "Citizen Two", null, Role.CITIZEN, null);
        citizen2.setId(20L);

        authority = new User("auth@test.com", "hash", "Authority Admin", null, Role.AUTHORITY, null);
        authority.setId(30L);

        worker = new User("worker@test.com", "hash", "Worker Joe", null, Role.WORKER, null);
        worker.setId(40L);

        testIssue = new Issue();
        testIssue.setId(100L);
        testIssue.setTrackingNumber("CF-2026-TEST");
        testIssue.setTitle("Water Leak");
        testIssue.setDescription("Broken tap");
        testIssue.setReporter(citizen1);
        testIssue.setStatus(IssueStatus.REPORT_CREATED);
        testIssue.setSeverity(IssueSeverity.MEDIUM);
        testIssue.setCalculatedPriority(IssuePriority.MEDIUM);
        testIssue.setLatitude(BigDecimal.valueOf(30.76));
        testIssue.setLongitude(BigDecimal.valueOf(76.57));
        testIssue.setImages(new ArrayList<>());
    }

    @Test
    @DisplayName("Authority can verify newly created report")
    void testAuthorityCanVerify() {
        when(issueRepository.save(any(Issue.class))).thenAnswer(i -> i.getArguments()[0]);

        Issue updated = lifecycleService.transitionStatus(
                testIssue, IssueStatus.VERIFIED, "Verified damage on site", null, authority
        );

        assertEquals(IssueStatus.VERIFIED, updated.getStatus());
        verify(statusHistoryRepository, times(1)).save(any(IssueStatusHistory.class));
    }

    @Test
    @DisplayName("Invalid transition jumping from REPORT_CREATED directly to RESOLVED is rejected")
    void testDirectJumpToResolvedRejected() {
        assertThrows(BadRequestException.class, () ->
                lifecycleService.transitionStatus(
                        testIssue, IssueStatus.RESOLVED, "Trying to skip", null, authority
                )
        );
    }

    @Test
    @DisplayName("Citizen cannot verify or reject authority-only initial review")
    void testCitizenCannotExecuteAuthorityActions() {
        assertThrows(UnauthorizedException.class, () ->
                lifecycleService.transitionStatus(
                        testIssue, IssueStatus.VERIFIED, "Citizen trying to verify", null, citizen1
                )
        );
    }

    @Test
    @DisplayName("Only original reporter can verify resolution or reopen; other citizens are unauthorized")
    void testUnauthorizedCitizenCannotReopenOrVerify() {
        testIssue.setStatus(IssueStatus.CITIZEN_VERIFICATION);

        // Citizen 2 is not the reporter (Citizen 1 is)
        assertThrows(UnauthorizedException.class, () ->
                lifecycleService.transitionStatus(
                        testIssue, IssueStatus.CLOSED, "Looks good", null, citizen2
                )
        );
    }

    @Test
    @DisplayName("Reopening an issue without a reason throws BadRequestException")
    void testReopenRequiresMandatoryReason() {
        testIssue.setStatus(IssueStatus.CITIZEN_VERIFICATION);

        assertThrows(BadRequestException.class, () ->
                lifecycleService.transitionStatus(
                        testIssue, IssueStatus.REOPENED, "", null, citizen1
                )
        );
    }

    @Test
    @DisplayName("Valid reopening by reporter transitions issue and increments reopen count")
    void testValidReopeningByReporter() {
        testIssue.setStatus(IssueStatus.CITIZEN_VERIFICATION);
        when(issueRepository.save(any(Issue.class))).thenAnswer(i -> i.getArguments()[0]);

        Issue updated = lifecycleService.transitionStatus(
                testIssue, IssueStatus.REOPENED, "Water pipe is still leaking actively", null, citizen1
        );

        assertEquals(IssueStatus.REOPENED, updated.getStatus());
        assertEquals(1, updated.getReopenCount());
        assertNull(updated.getResolvedAt());
    }
}
