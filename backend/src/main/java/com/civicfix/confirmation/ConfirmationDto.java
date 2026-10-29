package com.civicfix.confirmation;

import com.civicfix.user.UserDto;
import java.time.Instant;

public class ConfirmationDto {
    private Long id;
    private Long issueId;
    private UserDto citizen;
    private String note;
    private Instant createdAt;

    public ConfirmationDto() {}

    public static ConfirmationDto fromEntity(CommunityConfirmation confirmation) {
        if (confirmation == null) return null;
        ConfirmationDto dto = new ConfirmationDto();
        dto.id = confirmation.getId();
        dto.issueId = confirmation.getIssue().getId();
        dto.citizen = UserDto.fromEntity(confirmation.getCitizen());
        dto.note = confirmation.getNote();
        dto.createdAt = confirmation.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIssueId() { return issueId; }
    public void setIssueId(Long issueId) { this.issueId = issueId; }
    public UserDto getCitizen() { return citizen; }
    public void setCitizen(UserDto citizen) { this.citizen = citizen; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
