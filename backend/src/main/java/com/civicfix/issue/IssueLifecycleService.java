package com.civicfix.issue;

import com.civicfix.audit.AuditLogService;
import com.civicfix.common.BadRequestException;
import com.civicfix.common.ConflictException;
import com.civicfix.common.UnauthorizedException;
import com.civicfix.notification.NotificationService;
import com.civicfix.notification.NotificationType;
import com.civicfix.storage.IssueImage;
import com.civicfix.storage.IssueImageRepository;
import com.civicfix.storage.IssueImageType;
import com.civicfix.user.Role;
import com.civicfix.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class IssueLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(IssueLifecycleService.class);

    private final IssueRepository issueRepository;
    private final IssueStatusHistoryRepository statusHistoryRepository;
    private final IssueImageRepository issueImageRepository;
    private final IssuePriorityCalculator priorityCalculator;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public IssueLifecycleService(IssueRepository issueRepository,
                                 IssueStatusHistoryRepository statusHistoryRepository,
                                 IssueImageRepository issueImageRepository,
                                 IssuePriorityCalculator priorityCalculator,
                                 NotificationService notificationService,
                                 AuditLogService auditLogService) {
        this.issueRepository = issueRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.issueImageRepository = issueImageRepository;
        this.priorityCalculator = priorityCalculator;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    /**
     * Executes and audits a verified status transition in accordance with business workflow rules.
     */
    @Transactional
    public Issue transitionStatus(Issue issue, IssueStatus targetStatus, String reasonOrNotes, List<String> resolutionImageUrls, User actor) {
        IssueStatus currentStatus = issue.getStatus();

        if (currentStatus == targetStatus) {
            throw new ConflictException("Issue is already in status: " + targetStatus);
        }

        // Validate state transition validity
        validateTransition(currentStatus, targetStatus);

        // Validate actor authorization based on role and ownership
        validateActorPermission(issue, currentStatus, targetStatus, actor, reasonOrNotes);

        // Update timestamps and counters based on destination state
        if (targetStatus == IssueStatus.RESOLVED) {
            issue.setResolvedAt(Instant.now());
            // Automatically queue for citizen verification
            targetStatus = IssueStatus.CITIZEN_VERIFICATION;
        } else if (targetStatus == IssueStatus.CLOSED) {
            issue.setClosedAt(Instant.now());
        } else if (targetStatus == IssueStatus.REOPENED) {
            issue.setReopenCount(issue.getReopenCount() + 1);
            issue.setResolvedAt(null);
            issue.setClosedAt(null);
        }

        // Apply new status
        issue.setStatus(targetStatus);

        // Save resolution evidence photos if provided
        if (resolutionImageUrls != null && !resolutionImageUrls.isEmpty()) {
            for (String url : resolutionImageUrls) {
                IssueImage img = new IssueImage(issue, actor, url, IssueImageType.RESOLUTION_EVIDENCE, "Resolution evidence");
                issueImageRepository.save(img);
                issue.getImages().add(img);
            }
        }

        // Deterministically recompute priority score
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

        Issue updatedIssue = issueRepository.save(issue);

        // Record immutable status audit history
        IssueStatusHistory history = new IssueStatusHistory(
                updatedIssue,
                actor,
                currentStatus,
                targetStatus,
                StringUtils.hasText(reasonOrNotes) ? reasonOrNotes.trim() : "Status updated to " + targetStatus.getDisplayName()
        );
        statusHistoryRepository.save(history);

        // Audit log action
        auditLogService.logAction(
                "ISSUE_STATUS_TRANSITION",
                "Issue",
                updatedIssue.getId().toString(),
                actor.getId(),
                actor.getEmail(),
                actor.getRole().name(),
                String.format("Transitioned issue %s from %s to %s. Notes: %s",
                        updatedIssue.getTrackingNumber(), currentStatus, targetStatus, reasonOrNotes)
        );

        // Send real-time and persistent notifications
        dispatchStatusNotifications(updatedIssue, currentStatus, targetStatus, actor);

        log.info("Issue {} transitioned from {} to {} by user {}",
                updatedIssue.getTrackingNumber(), currentStatus, targetStatus, actor.getEmail());

        return updatedIssue;
    }

    private void validateTransition(IssueStatus from, IssueStatus to) {
        boolean valid = switch (from) {
            case REPORT_CREATED -> Set.of(IssueStatus.UNDER_REVIEW, IssueStatus.VERIFIED, IssueStatus.REJECTED).contains(to);
            case UNDER_REVIEW -> Set.of(IssueStatus.VERIFIED, IssueStatus.REJECTED).contains(to);
            case VERIFIED -> Set.of(IssueStatus.ASSIGNED).contains(to);
            case ASSIGNED -> Set.of(IssueStatus.ACCEPTED, IssueStatus.ASSIGNED).contains(to);
            case ACCEPTED -> Set.of(IssueStatus.IN_PROGRESS).contains(to);
            case IN_PROGRESS -> Set.of(IssueStatus.RESOLVED, IssueStatus.CITIZEN_VERIFICATION).contains(to);
            case RESOLVED, CITIZEN_VERIFICATION -> Set.of(IssueStatus.CLOSED, IssueStatus.REOPENED).contains(to);
            case REOPENED -> Set.of(IssueStatus.ASSIGNED, IssueStatus.UNDER_REVIEW).contains(to);
            case REJECTED, CLOSED -> false; // Terminal states
        };

        if (!valid) {
            throw new BadRequestException(
                    String.format("Invalid status transition from '%s' to '%s'. Primary workflow requires sequential advancement.",
                            from.getDisplayName(), to.getDisplayName())
            );
        }
    }

    private void validateActorPermission(Issue issue, IssueStatus from, IssueStatus to, User actor, String notes) {
        Role role = actor.getRole();

        // Admin has universal override capability
        if (role == Role.ADMIN) {
            return;
        }

        // Citizen (Reporter) actions: Citizen Verification -> Closed or Reopened
        if (to == IssueStatus.CLOSED || to == IssueStatus.REOPENED) {
            if (role != Role.CITIZEN && role != Role.ADMIN) {
                throw new UnauthorizedException("Only citizens or administrators can verify or reopen completed issues");
            }
            if (!issue.getReporter().getId().equals(actor.getId())) {
                throw new UnauthorizedException("Only the original citizen reporter can verify or reopen this issue");
            }
            if (to == IssueStatus.REOPENED && !StringUtils.hasText(notes)) {
                throw new BadRequestException("A mandatory reason is required when reopening an issue");
            }
            return;
        }

        // Worker actions: Accept, In Progress, Resolved
        if (to == IssueStatus.ACCEPTED || to == IssueStatus.IN_PROGRESS || to == IssueStatus.RESOLVED || to == IssueStatus.CITIZEN_VERIFICATION) {
            if (role != Role.WORKER && role != Role.AUTHORITY) {
                throw new UnauthorizedException("Only assigned workers or authorities can execute work status transitions");
            }
            if (role == Role.WORKER) {
                if (issue.getAssignedWorker() == null || !issue.getAssignedWorker().getId().equals(actor.getId())) {
                    throw new UnauthorizedException("You can only update tasks assigned specifically to you");
                }
            }
            return;
        }

        // Authority actions: Under Review, Verified, Rejected, Assigned
        if (to == IssueStatus.UNDER_REVIEW || to == IssueStatus.VERIFIED || to == IssueStatus.REJECTED || to == IssueStatus.ASSIGNED) {
            if (role != Role.AUTHORITY && role != Role.ADMIN) {
                throw new UnauthorizedException("Only campus authorities or administrators can verify, reject, or assign issues");
            }
            if (to == IssueStatus.REJECTED && !StringUtils.hasText(notes)) {
                throw new BadRequestException("A mandatory justification reason is required when rejecting a citizen report");
            }
        }
    }

    private void dispatchStatusNotifications(Issue issue, IssueStatus from, IssueStatus to, User actor) {
        String tracking = issue.getTrackingNumber();

        // 1. Notify Citizen Reporter
        if (!issue.getReporter().getId().equals(actor.getId())) {
            String title = "Issue Status Updated: " + to.getDisplayName();
            String message = String.format("Your reported issue '%s' (%s) is now %s.",
                    issue.getTitle(), tracking, to.getDisplayName());
            notificationService.createAndSend(issue.getReporter(), title, message, NotificationType.STATUS_CHANGE, issue);
        }

        // 2. Notify Assigned Worker
        if (issue.getAssignedWorker() != null && !issue.getAssignedWorker().getId().equals(actor.getId())) {
            String title = "Task Update: " + to.getDisplayName();
            String message = String.format("Assigned task '%s' (%s) status changed to %s.",
                    issue.getTitle(), tracking, to.getDisplayName());
            notificationService.createAndSend(issue.getAssignedWorker(), title, message, NotificationType.STATUS_CHANGE, issue);
        }
    }
}
