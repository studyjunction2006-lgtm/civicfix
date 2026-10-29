package com.civicfix.analytics;

import com.civicfix.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "Endpoints for platform metrics, KPI calculations, and resolution performance")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/public")
    @Operation(summary = "Get high-level statistics for public landing page")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPublicStats() {
        AnalyticsSummaryDto full = analyticsService.getPlatformAnalytics();
        Map<String, Object> publicStats = Map.of(
                "totalIssues", full.getTotalIssues(),
                "resolvedIssues", full.getResolvedIssues() + full.getClosedIssues(),
                "resolutionRate", full.getResolutionRate(),
                "averageResolutionHours", full.getAverageResolutionHours()
        );
        return ResponseEntity.ok(ApiResponse.ok(publicStats));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get comprehensive analytics summary for dashboard charts and reports")
    public ResponseEntity<ApiResponse<AnalyticsSummaryDto>> getAnalyticsSummary() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getPlatformAnalytics()));
    }
}
