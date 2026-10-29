package com.civicfix.issue;

import com.civicfix.audit.AuditLogService;
import com.civicfix.category.IssueCategory;
import com.civicfix.category.CategoryRepository;
import com.civicfix.common.PagedResponse;
import com.civicfix.common.ResourceNotFoundException;
import com.civicfix.common.UnauthorizedException;
import com.civicfix.notification.NotificationService;
import com.civicfix.notification.NotificationType;
import com.civicfix.storage.IssueImage;
import com.civicfix.storage.IssueImageRepository;
import com.civicfix.storage.IssueImageType;
import com.civicfix.user.Role;
import com.civicfix.user.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final CategoryRepository categoryRepository;
    private final IssueStatusHistoryRepository statusHistoryRepository;
    private final IssueImageRepository issueImageRepository;
    private final IssuePriorityCalculator priorityCalculator;
    private final IssueLifecycleService issueLifecycleService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public IssueService(IssueRepository issueRepository,
                        CategoryRepository categoryRepository,
                        IssueStatusHistoryRepository statusHistoryRepository,
                        IssueImageRepository issueImageRepository,
                        IssuePriorityCalculator priorityCalculator,
                        IssueLifecycleService issueLifecycleService,
                        NotificationService notificationService,
                        AuditLogService auditLogService) {
        this.issueRepository = issueRepository;
        this.categoryRepository = categoryRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.issueImageRepository = issueImageRepository;
        this.priorityCalculator = priorityCalculator;
        this.issueLifecycleService = issueLifecycleService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public IssueDto createIssue(CreateIssueRequest request, User reporter) {
        IssueCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        String trackingNumber = generateUniqueTrackingNumber();

        Issue issue = new Issue();
        issue.setTrackingNumber(trackingNumber);
        issue.setTitle(request.getTitle().trim());
        issue.setDescription(request.getDescription().trim());
        issue.setCategory(category);
        issue.setDepartment(category.getDepartment());
        issue.setReporter(reporter);
        issue.setStatus(IssueStatus.REPORT_CREATED);
        issue.setSeverity(request.getSeverity() != null ? request.getSeverity() : IssueSeverity.MEDIUM);
        issue.setLatitude(request.getLatitude());
        issue.setLongitude(request.getLongitude());
        issue.setAddressText(request.getAddressText());

        // SLA Due Date calculation based on Category SLA hours
        int slaHours = category.getSlaHours() > 0 ? category.getSlaHours() : 48;
        issue.setDueDate(Instant.now().plus(slaHours, ChronoUnit.HOURS));

        // Initial priority calculation
        var priorityResult = priorityCalculator.calculate(
                issue.getSeverity(),
                0,
                Instant.now(),
                issue.getDueDate(),
                0,
                IssueStatus.REPORT_CREATED
        );
        issue.setPriorityScore(priorityResult.score());
        issue.setCalculatedPriority(priorityResult.priority());

        Issue saved = issueRepository.save(issue);

        // Attach photos if supplied
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (String url : request.getImageUrls()) {
                if (StringUtils.hasText(url)) {
                    IssueImage img = new IssueImage(saved, reporter, url.trim(), IssueImageType.REPORT_EVIDENCE, "Report photo");
                    issueImageRepository.save(img);
                    saved.getImages().add(img);
                }
            }
        }

        // Record initial status history
        IssueStatusHistory initialHistory = new IssueStatusHistory(
                saved,
                reporter,
                null,
                IssueStatus.REPORT_CREATED,
                "Issue reported by citizen: " + reporter.getFullName()
        );
        statusHistoryRepository.save(initialHistory);

        // Audit log action
        auditLogService.logAction(
                "CREATE_ISSUE",
                "Issue",
                saved.getId().toString(),
                reporter.getId(),
                reporter.getEmail(),
                reporter.getRole().name(),
                String.format("Created issue %s: %s", saved.getTrackingNumber(), saved.getTitle())
        );

        // Notify reporter of successful submission
        notificationService.createAndSend(
                reporter,
                "Issue Reported: " + saved.getTrackingNumber(),
                "Your report has been submitted. Tracking number is " + saved.getTrackingNumber(),
                NotificationType.STATUS_CHANGE,
                saved
        );

        return IssueDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public IssueDto getIssueById(Long id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "id", id));
        return IssueDto.fromEntity(issue);
    }

    @Transactional(readOnly = true)
    public IssueDto getIssueByTrackingNumber(String trackingNumber) {
        Issue issue = issueRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "trackingNumber", trackingNumber));
        return IssueDto.fromEntity(issue);
    }

    @Transactional(readOnly = true)
    public PagedResponse<IssueDto> searchIssues(String query,
                                               IssueStatus status,
                                               IssuePriority priority,
                                               IssueSeverity severity,
                                               Long categoryId,
                                               Long departmentId,
                                               Long reporterId,
                                               Long workerId,
                                               int page,
                                               int size,
                                               String sortBy,
                                               String sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir != null ? sortDir : "DESC"),
                StringUtils.hasText(sortBy) ? sortBy : "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Issue> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(query)) {
                String searchLike = "%" + query.trim().toLowerCase() + "%";
                Predicate titlePred = cb.like(cb.lower(root.get("title")), searchLike);
                Predicate descPred = cb.like(cb.lower(root.get("description")), searchLike);
                Predicate trackPred = cb.like(cb.lower(root.get("trackingNumber")), searchLike);
                Predicate addressPred = cb.like(cb.lower(root.get("addressText")), searchLike);
                predicates.add(cb.or(titlePred, descPred, trackPred, addressPred));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (priority != null) {
                predicates.add(cb.equal(root.get("calculatedPriority"), priority));
            }

            if (severity != null) {
                predicates.add(cb.equal(root.get("severity"), severity));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }

            if (reporterId != null) {
                predicates.add(cb.equal(root.get("reporter").get("id"), reporterId));
            }

            if (workerId != null) {
                predicates.add(cb.equal(root.get("assignedWorker").get("id"), workerId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Issue> paged = issueRepository.findAll(spec, pageable);
        List<IssueDto> dtoList = paged.getContent().stream().map(IssueDto::fromEntity).toList();

        return new PagedResponse<>(dtoList, paged.getNumber(), paged.getSize(), paged.getTotalElements(), paged.getTotalPages(), paged.isLast());
    }

    @Transactional
    public IssueDto transitionStatus(Long id, StatusTransitionRequest request, User actor) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "id", id));

        Issue updated = issueLifecycleService.transitionStatus(
                issue,
                request.getNewStatus(),
                request.getNotes(),
                request.getResolutionImageUrls(),
                actor
        );

        return IssueDto.fromEntity(updated);
    }

    @Transactional
    public IssueDto overridePriority(Long id, PriorityOverrideRequest request, User actor) {
        if (actor.getRole() != Role.AUTHORITY && actor.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only campus authorities or administrators can override priority scores");
        }

        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "id", id));

        IssuePriority previousPriority = issue.getCalculatedPriority();
        issue.setCalculatedPriority(request.getPriority());

        // Map nominal score based on override
        double nominalScore = switch (request.getPriority()) {
            case LOW -> 20.0;
            case MEDIUM -> 45.0;
            case HIGH -> 70.0;
            case CRITICAL -> 95.0;
        };
        issue.setPriorityScore(nominalScore);

        Issue saved = issueRepository.save(issue);

        // Record history entry for auditability
        IssueStatusHistory history = new IssueStatusHistory(
                saved,
                actor,
                saved.getStatus(),
                saved.getStatus(),
                String.format("Priority manually overridden from %s to %s by %s. Reason: %s",
                        previousPriority, request.getPriority(), actor.getFullName(), request.getReason())
        );
        statusHistoryRepository.save(history);

        auditLogService.logAction(
                "PRIORITY_OVERRIDE",
                "Issue",
                saved.getId().toString(),
                actor.getId(),
                actor.getEmail(),
                actor.getRole().name(),
                String.format("Priority overridden to %s for issue %s: %s", request.getPriority(), saved.getTrackingNumber(), request.getReason())
        );

        return IssueDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<IssueDto> getPublicIssuesForMap() {
        return issueRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(IssueDto::fromEntity)
                .toList();
    }

    private String generateUniqueTrackingNumber() {
        int currentYear = Year.now().getValue();
        for (int i = 0; i < 10; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(1000, 9999);
            String tracking = String.format("CF-%d-%04d", currentYear, randomNum);
            if (!issueRepository.existsByTrackingNumber(tracking)) {
                return tracking;
            }
        }
        return "CF-" + currentYear + "-" + System.currentTimeMillis() % 10000;
    }
}
