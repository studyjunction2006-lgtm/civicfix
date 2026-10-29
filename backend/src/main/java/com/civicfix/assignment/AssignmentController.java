package com.civicfix.assignment;

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
@RequestMapping("/api/v1/assignments")
@Tag(name = "Worker Assignments", description = "Endpoints for managing task dispatch, worker acceptance, and progress")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final UserRepository userRepository;

    public AssignmentController(AssignmentService assignmentService, UserRepository userRepository) {
        this.assignmentService = assignmentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/issues/{issueId}")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    @Operation(summary = "Assign a worker and department to an issue")
    public ResponseEntity<ApiResponse<AssignmentDto>> assignWorker(
            @PathVariable Long issueId,
            @Valid @RequestBody AssignWorkerRequest request) {

        UserPrincipal principal = SecurityUtils.getCurrentUser();
        User assigner = userRepository.getReferenceById(principal.getId());

        AssignmentDto dto = assignmentService.assignWorker(issueId, request, assigner);
        return new ResponseEntity<>(ApiResponse.ok("Worker assigned successfully", dto), HttpStatus.CREATED);
    }

    @PostMapping("/{assignmentId}/respond")
    @PreAuthorize("hasAnyRole('WORKER', 'ADMIN')")
    @Operation(summary = "Worker accepts or declines an assignment with reasons")
    public ResponseEntity<ApiResponse<AssignmentDto>> respondToAssignment(
            @PathVariable Long assignmentId,
            @Valid @RequestBody AssignmentActionRequest request) {

        UserPrincipal principal = SecurityUtils.getCurrentUser();
        User worker = userRepository.getReferenceById(principal.getId());

        AssignmentDto dto = assignmentService.respondToAssignment(assignmentId, request, worker);
        return ResponseEntity.ok(ApiResponse.ok("Response recorded successfully", dto));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('WORKER', 'ADMIN')")
    @Operation(summary = "Get tasks assigned to the current worker")
    public ResponseEntity<ApiResponse<PagedResponse<AssignmentDto>>> getMyAssignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        UserPrincipal principal = SecurityUtils.getCurrentUser();
        User worker = userRepository.getReferenceById(principal.getId());

        PagedResponse<AssignmentDto> response = assignmentService.getWorkerAssignments(worker, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/issues/{issueId}")
    @Operation(summary = "Get assignment history for an issue")
    public ResponseEntity<ApiResponse<List<AssignmentDto>>> getIssueAssignments(@PathVariable Long issueId) {
        return ResponseEntity.ok(ApiResponse.ok(assignmentService.getAssignmentsForIssue(issueId)));
    }
}
