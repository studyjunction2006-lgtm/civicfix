package com.civicfix.issue;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class CreateIssueRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 5000, message = "Description must provide sufficient detail (10-5000 characters)")
    private String description;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Severity is required")
    private IssueSeverity severity = IssueSeverity.MEDIUM;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;

    private String addressText;

    @Size(max = 5, message = "You can attach at most 5 photos per report")
    private List<String> imageUrls;

    public CreateIssueRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public IssueSeverity getSeverity() { return severity; }
    public void setSeverity(IssueSeverity severity) { this.severity = severity; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public String getAddressText() { return addressText; }
    public void setAddressText(String addressText) { this.addressText = addressText; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}
