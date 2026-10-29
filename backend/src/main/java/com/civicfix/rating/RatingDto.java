package com.civicfix.rating;

import com.civicfix.user.UserDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class RatingDto {
    private Long id;
    private Long issueId;
    private UserDto citizen;
    private UserDto worker;
    private int rating;
    private String feedback;
    private Instant createdAt;

    public RatingDto() {}

    public static RatingDto fromEntity(ResolutionRating r) {
        if (r == null) return null;
        RatingDto dto = new RatingDto();
        dto.id = r.getId();
        dto.issueId = r.getIssue().getId();
        dto.citizen = UserDto.fromEntity(r.getCitizen());
        dto.worker = UserDto.fromEntity(r.getWorker());
        dto.rating = r.getRating();
        dto.feedback = r.getFeedback();
        dto.createdAt = r.getCreatedAt();
        return dto;
    }

    public static class CreateRatingRequest {
        @NotNull(message = "Rating score is required")
        @Min(value = 1, message = "Rating must be at least 1 star")
        @Max(value = 5, message = "Rating cannot exceed 5 stars")
        private Integer rating;

        private String feedback;

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIssueId() { return issueId; }
    public void setIssueId(Long issueId) { this.issueId = issueId; }
    public UserDto getCitizen() { return citizen; }
    public void setCitizen(UserDto citizen) { this.citizen = citizen; }
    public UserDto getWorker() { return worker; }
    public void setWorker(UserDto worker) { this.worker = worker; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
