package com.civicfix.notification;

import com.civicfix.common.ApiResponse;
import com.civicfix.common.PagedResponse;
import com.civicfix.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "REST fallback and endpoints for notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get paginated notifications for current user")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationService.NotificationDto>>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<NotificationService.NotificationDto> paged = notificationService.getUserNotificationsPaged(userId, page, size);
        PagedResponse<NotificationService.NotificationDto> response = new PagedResponse<>(
                paged.getContent(), paged.getNumber(), paged.getSize(), paged.getTotalElements(), paged.getTotalPages(), paged.isLast()
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get count of unread notifications")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("unreadCount", count)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", null));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read for current user")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", null));
    }
}
