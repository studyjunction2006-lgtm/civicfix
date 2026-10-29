package com.civicfix.issue;

import com.civicfix.common.ApiResponse;
import com.civicfix.common.PagedResponse;
import com.civicfix.security.SecurityUtils;
import com.civicfix.security.UserPrincipal;
import com.civicfix.user.User;
import com.civicfix.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/issues")
@Tag(name = "Issues", description = "Endpoints for creating, managing, tracking, and resolving community issues")
public class IssueController {

    private final IssueService issueService;
    private final UserRepository userRepository;

    public IssueController(IssueService issueService, UserRepository userRepository) {
        this.issueService = issueService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @Operation(summary = "Submit a new issue report with GPS coordinates and photos")
    public ResponseEntity<ApiResponse<IssueDto>> createIssue(@Valid @RequestBody CreateIssueRequest request) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        User reporter = userRepository.getReferenceById(principal.getId());

        IssueDto created = issueService.createIssue(request, reporter);
        return new ResponseEntity<>(ApiResponse.ok("Issue reported successfully", created), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Search and filter issues with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<IssueDto>>> searchIssues(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) IssuePriority priority,
            @RequestParam(required = false) IssueSeverity severity,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long reporterId,
            @RequestParam(required = false) Long workerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        PagedResponse<IssueDto> response = issueService.searchIssues(
                query, status, priority, severity, categoryId, departmentId, reporterId, workerId, page, size, sortBy, sortDir
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/my-reports")
    @Operation(summary = "Get issues reported by the authenticated user")
    public ResponseEntity<ApiResponse<PagedResponse<IssueDto>>> getMyReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UserPrincipal principal = SecurityUtils.getCurrentUser();
        PagedResponse<IssueDto> response = issueService.searchIssues(
                null, null, null, null, null, null, principal.getId(), null, page, size, "createdAt", "DESC"
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full issue details by ID")
    public ResponseEntity<ApiResponse<IssueDto>> getIssueById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(issueService.getIssueById(id)));
    }

    @GetMapping("/tracking/{trackingNumber}")
    @Operation(summary = "Get issue details by tracking number (e.g., CF-2026-0001)")
    public ResponseEntity<ApiResponse<IssueDto>> getIssueByTrackingNumber(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(ApiResponse.ok(issueService.getIssueByTrackingNumber(trackingNumber)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Advance or update issue status following workflow transition rules")
    public ResponseEntity<ApiResponse<IssueDto>> transitionStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusTransitionRequest request) {

        UserPrincipal principal = SecurityUtils.getCurrentUser();
        User actor = userRepository.getReferenceById(principal.getId());

        IssueDto updated = issueService.transitionStatus(id, request, actor);
        return ResponseEntity.ok(ApiResponse.ok("Issue status updated successfully", updated));
    }

    @PatchMapping("/{id}/priority")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    @Operation(summary = "Authority or Admin manually overrides calculated priority with justification")
    public ResponseEntity<ApiResponse<IssueDto>> overridePriority(
            @PathVariable Long id,
            @Valid @RequestBody PriorityOverrideRequest request) {

        UserPrincipal principal = SecurityUtils.getCurrentUser();
        User actor = userRepository.getReferenceById(principal.getId());

        IssueDto updated = issueService.overridePriority(id, request, actor);
        return ResponseEntity.ok(ApiResponse.ok("Priority updated successfully", updated));
    }

    @GetMapping("/public/map")
    @Operation(summary = "Get publicly visible issues for OpenStreetMap/Leaflet rendering")
    public ResponseEntity<ApiResponse<List<IssueDto>>> getPublicMapIssues() {
        return ResponseEntity.ok(ApiResponse.ok(issueService.getPublicIssuesForMap()));
    }

    @GetMapping("/public/recent")
    @Operation(summary = "Get recent public issues for landing page explore feed")
    public ResponseEntity<ApiResponse<List<IssueDto>>> getRecentPublicIssues() {
        return ResponseEntity.ok(ApiResponse.ok(issueService.getPublicIssuesForMap()));
    }
}
