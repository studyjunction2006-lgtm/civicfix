package com.civicfix.rating;

import com.civicfix.common.ApiResponse;
import com.civicfix.security.SecurityUtils;
import com.civicfix.security.UserPrincipal;
import com.civicfix.user.User;
import com.civicfix.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ratings")
@Tag(name = "Resolution Ratings", description = "Endpoints for citizen work satisfaction ratings and feedback")
public class RatingController {

    private final RatingService ratingService;
    private final UserRepository userRepository;

    public RatingController(RatingService ratingService, UserRepository userRepository) {
        this.ratingService = ratingService;
        this.userRepository = userRepository;
    }

    @PostMapping("/issues/{issueId}")
    @Operation(summary = "Citizen rates the completed work (1-5 stars with feedback)")
    public ResponseEntity<ApiResponse<RatingDto>> submitRating(
            @PathVariable Long issueId,
            @Valid @RequestBody RatingDto.CreateRatingRequest request) {

        UserPrincipal principal = SecurityUtils.getCurrentUser();
        User citizen = userRepository.getReferenceById(principal.getId());

        RatingDto dto = ratingService.submitRating(issueId, request, citizen);
        return new ResponseEntity<>(ApiResponse.ok("Thank you! Your rating has been recorded", dto), HttpStatus.CREATED);
    }

    @GetMapping("/issues/{issueId}")
    @Operation(summary = "Get rating submitted for an issue")
    public ResponseEntity<ApiResponse<RatingDto>> getRating(@PathVariable Long issueId) {
        return ResponseEntity.ok(ApiResponse.ok(ratingService.getRatingForIssue(issueId)));
    }
}
