package com.civicfix.rating;

import com.civicfix.audit.AuditLogService;
import com.civicfix.common.BadRequestException;
import com.civicfix.common.ConflictException;
import com.civicfix.common.ResourceNotFoundException;
import com.civicfix.common.UnauthorizedException;
import com.civicfix.issue.Issue;
import com.civicfix.issue.IssueRepository;
import com.civicfix.issue.IssueStatus;
import com.civicfix.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final IssueRepository issueRepository;
    private final AuditLogService auditLogService;

    public RatingService(RatingRepository ratingRepository, IssueRepository issueRepository, AuditLogService auditLogService) {
        this.ratingRepository = ratingRepository;
        this.issueRepository = issueRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public RatingDto submitRating(Long issueId, RatingDto.CreateRatingRequest request, User citizen) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "id", issueId));

        if (!issue.getReporter().getId().equals(citizen.getId())) {
            throw new UnauthorizedException("Only the citizen who originally reported this issue can rate the resolution");
        }

        if (issue.getStatus() != IssueStatus.CITIZEN_VERIFICATION && issue.getStatus() != IssueStatus.CLOSED && issue.getStatus() != IssueStatus.RESOLVED) {
            throw new BadRequestException("Ratings can only be submitted for completed or resolved issues");
        }

        if (ratingRepository.existsByIssueIdAndCitizenId(issueId, citizen.getId())) {
            throw new ConflictException("You have already submitted a rating for this issue");
        }

        ResolutionRating rating = new ResolutionRating(
                issue,
                citizen,
                issue.getAssignedWorker(),
                request.getRating(),
                request.getFeedback()
        );

        ResolutionRating saved = ratingRepository.save(rating);

        auditLogService.logAction(
                "SUBMIT_RATING",
                "Issue",
                issue.getId().toString(),
                citizen.getId(),
                citizen.getEmail(),
                citizen.getRole().name(),
                String.format("Rated resolution for issue %s: %d stars", issue.getTrackingNumber(), request.getRating())
        );

        return RatingDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public RatingDto getRatingForIssue(Long issueId) {
        return ratingRepository.findByIssueId(issueId)
                .map(RatingDto::fromEntity)
                .orElse(null);
    }
}
