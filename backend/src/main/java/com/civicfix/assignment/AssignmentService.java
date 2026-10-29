package com.civicfix.assignment;

import com.civicfix.audit.AuditLogService;
import com.civicfix.common.BadRequestException;
import com.civicfix.common.PagedResponse;
import com.civicfix.common.ResourceNotFoundException;
import com.civicfix.common.UnauthorizedException;
import com.civicfix.department.Department;
import com.civicfix.department.DepartmentRepository;
import com.civicfix.issue.Issue;
import com.civicfix.issue.IssueLifecycleService;
import com.civicfix.issue.IssueRepository;
import com.civicfix.issue.IssueStatus;
import com.civicfix.notification.NotificationService;
import com.civicfix.notification.NotificationType;
import com.civicfix.user.Role;
import com.civicfix.user.User;
import com.civicfix.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final IssueLifecycleService issueLifecycleService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             IssueRepository issueRepository,
                             UserRepository userRepository,
                             DepartmentRepository departmentRepository,
                             IssueLifecycleService issueLifecycleService,
                             NotificationService notificationService,
                             AuditLogService auditLogService) {
        this.assignmentRepository = assignmentRepository;
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.issueLifecycleService = issueLifecycleService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AssignmentDto assignWorker(Long issueId, AssignWorkerRequest request, User assigner) {
        if (assigner.getRole() != Role.AUTHORITY && assigner.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only campus authorities or administrators can assign workers");
        }

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "id", issueId));

        User worker = userRepository.findById(request.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker", "id", request.getWorkerId()));

        if (worker.getRole() != Role.WORKER) {
            throw new BadRequestException("Target user is not registered as a field worker");
        }

        // If department is provided, update issue department
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
            issue.setDepartment(dept);
        }

        // Mark any previous pending assignment as REASSIGNED
        assignmentRepository.findFirstByIssueIdAndStatus(issueId, AssignmentStatus.PENDING)
                .ifPresent(prev -> {
                    prev.setStatus(AssignmentStatus.REASSIGNED);
                    assignmentRepository.save(prev);
                });

        issue.setAssignedWorker(worker);

        // Transition issue status to ASSIGNED if not already
        if (issue.getStatus() != IssueStatus.ASSIGNED) {
            issueLifecycleService.transitionStatus(
                    issue,
                    IssueStatus.ASSIGNED,
                    "Assigned to worker " + worker.getFullName() + ". Notes: " + (request.getNotes() != null ? request.getNotes() : ""),
                    null,
                    assigner
            );
        } else {
            issueRepository.save(issue);
        }

        WorkerAssignment assignment = new WorkerAssignment(issue, worker, assigner);
        assignment.setWorkerNotes(request.getNotes());
        WorkerAssignment savedAssignment = assignmentRepository.save(assignment);

        // Notify Worker
        String title = "New Task Assignment: " + issue.getTrackingNumber();
        String message = String.format("You have been assigned to issue '%s' (%s). Please review and accept.",
                issue.getTitle(), issue.getTrackingNumber());
        notificationService.createAndSend(worker, title, message, NotificationType.TASK_ASSIGNED, issue);

        auditLogService.logAction(
                "ASSIGN_WORKER",
                "Issue",
                issue.getId().toString(),
                assigner.getId(),
                assigner.getEmail(),
                assigner.getRole().name(),
                String.format("Assigned issue %s to worker %s (%s)", issue.getTrackingNumber(), worker.getFullName(), worker.getEmail())
        );

        return AssignmentDto.fromEntity(savedAssignment);
    }

    @Transactional
    public AssignmentDto respondToAssignment(Long assignmentId, AssignmentActionRequest request, User worker) {
        WorkerAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkerAssignment", "id", assignmentId));

        if (!assignment.getWorker().getId().equals(worker.getId()) && worker.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You can only respond to your own assignments");
        }

        Issue issue = assignment.getIssue();

        if (request.getAction() == AssignmentActionRequest.Action.ACCEPT) {
            assignment.setStatus(AssignmentStatus.ACCEPTED);
            assignment.setAcceptedAt(Instant.now());
            if (StringUtils.hasText(request.getWorkerNotes())) {
                assignment.setWorkerNotes(request.getWorkerNotes());
            }

            // Transition issue status to ACCEPTED
            issueLifecycleService.transitionStatus(
                    issue,
                    IssueStatus.ACCEPTED,
                    "Worker accepted assignment. " + (request.getWorkerNotes() != null ? request.getWorkerNotes() : ""),
                    null,
                    worker
            );

            // Notify Assigning Authority
            notificationService.createAndSend(
                    assignment.getAssignedBy(),
                    "Assignment Accepted: " + issue.getTrackingNumber(),
                    String.format("Worker %s accepted task '%s'.", worker.getFullName(), issue.getTitle()),
                    NotificationType.ASSIGNMENT_ACCEPTED,
                    issue
            );

        } else if (request.getAction() == AssignmentActionRequest.Action.DECLINE) {
            if (!StringUtils.hasText(request.getDeclineReason())) {
                throw new BadRequestException("A mandatory decline reason must be specified");
            }

            assignment.setStatus(AssignmentStatus.DECLINED);
            assignment.setDeclineReason(request.getDeclineReason());

            // Revert issue assigned worker so authority can reassign
            issue.setAssignedWorker(null);
            issue.setStatus(IssueStatus.VERIFIED);
            issueRepository.save(issue);

            // Notify Assigning Authority
            notificationService.createAndSend(
                    assignment.getAssignedBy(),
                    "Assignment Declined: " + issue.getTrackingNumber(),
                    String.format("Worker %s declined task '%s'. Reason: %s", worker.getFullName(), issue.getTitle(), request.getDeclineReason()),
                    NotificationType.ASSIGNMENT_DECLINED,
                    issue
            );
        }

        WorkerAssignment updated = assignmentRepository.save(assignment);
        return AssignmentDto.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AssignmentDto> getWorkerAssignments(User worker, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<WorkerAssignment> paged = assignmentRepository.findByWorkerIdOrderByAssignedAtDesc(worker.getId(), pageable);
        List<AssignmentDto> dtoList = paged.getContent().stream().map(AssignmentDto::fromEntity).toList();
        return new PagedResponse<>(dtoList, paged.getNumber(), paged.getSize(), paged.getTotalElements(), paged.getTotalPages(), paged.isLast());
    }

    @Transactional(readOnly = true)
    public List<AssignmentDto> getAssignmentsForIssue(Long issueId) {
        return assignmentRepository.findByIssueIdOrderByAssignedAtDesc(issueId).stream()
                .map(AssignmentDto::fromEntity)
                .toList();
    }
}
