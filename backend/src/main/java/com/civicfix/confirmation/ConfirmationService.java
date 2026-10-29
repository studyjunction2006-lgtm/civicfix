package com.civicfix.confirmation;

import com.civicfix.audit.AuditLogService;
import com.civicfix.common.ConflictException;
import com.civicfix.common.ResourceNotFoundException;
import com.civicfix.issue.Issue;
import com.civicfix.issue.IssuePriorityCalculator;
import com.civicfix.issue.IssueRepository;
import com.civicfix.notification.NotificationService;
import com.civicfix.notification.NotificationType;
import com.civicfix.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConfirmationService {

    private final ConfirmationRepository confirmationRepository;
    private final IssueRepository issueRepository;
    private final IssuePriorityCalculator priorityCalculator;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public ConfirmationService(ConfirmationRepository confirmationRepository,
                               IssueRepository issueRepository,
                               IssuePriorityCalculator priorityCalculator,
                               NotificationService notificationService,
                               AuditLogService auditLogService) {
        this.confirmationRepository = confirmationRepository;
        this.issueRepository = issueRepository;
        this.priorityCalculator = priorityCalculator;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ConfirmationDto confirmIssue(Long issueId, String note, User citizen) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "id", issueId));

        // Enforce duplicate prevention
        if (confirmationRepository.existsByIssueIdAndCitizenId(issueId, citizen.getId())) {
            throw new ConflictException("You have already confirmed this issue report.");
        }

        CommunityConfirmation confirmation = new CommunityConfirmation(issue, citizen, note);
        CommunityConfirmation saved = confirmationRepository.save(confirmation);

        // Update confirmation count
        issue.setConfirmationCount(issue.getConfirmationCount() + 1);

        // Recalculate deterministic priority score with new confirmation weight
        var priorityResult = priorityCalculator.calculate(
                issue.getSeverity(),
                issue.getConfirmationCount(),
                issue.getCreatedAt(),
                issue.getDueDate(),
                issue.getReopenCount(),
                issue.getStatus()
        );
        issue.setPriorityScore(priorityResult.score());
        issue.setCalculatedPriority(priorityResult.priority());
        issueRepository.save(issue);

        // Notify original reporter
        if (!issue.getReporter().getId().equals(citizen.getId())) {
            String title = "New Community Confirmation";
            String message = String.format("%s confirmed your reported issue '%s'. Priority score escalated.",
                    citizen.getFullName(), issue.getTitle());
            notificationService.createAndSend(issue.getReporter(), title, message, NotificationType.COMMUNITY_CONFIRMATION, issue);
        }

        auditLogService.logAction(
                "COMMUNITY_CONFIRMATION",
                "Issue",
                issue.getId().toString(),
                citizen.getId(),
                citizen.getEmail(),
                citizen.getRole().name(),
                String.format("Citizen confirmed issue %s. Total confirmations now: %d", issue.getTrackingNumber(), issue.getConfirmationCount())
        );

        return ConfirmationDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public boolean hasUserConfirmed(Long issueId, Long citizenId) {
        return confirmationRepository.existsByIssueIdAndCitizenId(issueId, citizenId);
    }

    @Transactional(readOnly = true)
    public List<ConfirmationDto> getConfirmationsForIssue(Long issueId) {
        return confirmationRepository.findByIssueIdOrderByCreatedAtDesc(issueId).stream()
                .map(ConfirmationDto::fromEntity)
                .toList();
    }
}
