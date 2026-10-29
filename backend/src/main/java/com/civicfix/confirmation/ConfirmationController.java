package com.civicfix.confirmation;

import com.civicfix.common.ApiResponse;
import com.civicfix.security.SecurityUtils;
import com.civicfix.security.UserPrincipal;
import com.civicfix.user.User;
import com.civicfix.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/confirmations")
@Tag(name = "Community Confirmations", description = "Endpoints for citizen validation and duplicate prevention")
public class ConfirmationController {

    private final ConfirmationService confirmationService;
    private final UserRepository userRepository;

    public ConfirmationController(ConfirmationService confirmationService, UserRepository userRepository) {
        this.confirmationService = confirmationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/issues/{issueId}")
    @Operation(summary = "Citizen confirms an existing issue (prevents duplicate submissions)")
    public ResponseEntity<ApiResponse<ConfirmationDto>> confirmIssue(
            @PathVariable Long issueId,
            @RequestBody(required = false) Map<String, String> body) {

        UserPrincipal principal = SecurityUtils.getCurrentUser();
        User citizen = userRepository.getReferenceById(principal.getId());

        String note = (body != null) ? body.get("note") : null;
        ConfirmationDto dto = confirmationService.confirmIssue(issueId, note, citizen);

        return new ResponseEntity<>(ApiResponse.ok("Issue confirmed successfully", dto), HttpStatus.CREATED);
    }

    @GetMapping("/issues/{issueId}")
    @Operation(summary = "Get list of community confirmations for an issue")
    public ResponseEntity<ApiResponse<List<ConfirmationDto>>> getConfirmations(@PathVariable Long issueId) {
        return ResponseEntity.ok(ApiResponse.ok(confirmationService.getConfirmationsForIssue(issueId)));
    }

    @GetMapping("/issues/{issueId}/check")
    @Operation(summary = "Check if currently logged in user has already confirmed this issue")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUserConfirmed(@PathVariable Long issueId) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        boolean confirmed = confirmationService.hasUserConfirmed(issueId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("confirmed", confirmed)));
    }
}
