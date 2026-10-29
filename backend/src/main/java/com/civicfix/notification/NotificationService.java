package com.civicfix.notification;

import com.civicfix.issue.Issue;
import com.civicfix.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public NotificationDto createAndSend(User recipient, String title, String message, NotificationType type, Issue issue) {
        if (recipient == null) return null;

        Notification notification = new Notification(recipient, title, message, type, issue);
        Notification saved = notificationRepository.save(notification);
        NotificationDto dto = NotificationDto.fromEntity(saved);

        // Push via WebSocket / STOMP
        try {
            messagingTemplate.convertAndSend("/topic/notifications/" + recipient.getId(), dto);
        } catch (Exception e) {
            log.warn("WebSocket dispatch failed for user {}: {}", recipient.getId(), e.getMessage());
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(Long recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId).stream()
                .map(NotificationDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotificationsPaged(Long recipientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable)
                .map(NotificationDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long recipientId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getRecipient().getId().equals(recipientId)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    @Transactional
    public void markAllAsRead(Long recipientId) {
        List<Notification> list = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
        list.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(list);
    }

    public static class NotificationDto {
        private Long id;
        private Long recipientId;
        private String title;
        private String message;
        private NotificationType notificationType;
        private Long issueId;
        private String issueTrackingNumber;
        private boolean isRead;
        private Instant createdAt;

        public static NotificationDto fromEntity(Notification n) {
            NotificationDto dto = new NotificationDto();
            dto.id = n.getId();
            dto.recipientId = n.getRecipient().getId();
            dto.title = n.getTitle();
            dto.message = n.getMessage();
            dto.notificationType = n.getNotificationType();
            dto.isRead = n.isRead();
            dto.createdAt = n.getCreatedAt();
            if (n.getIssue() != null) {
                dto.issueId = n.getIssue().getId();
                dto.issueTrackingNumber = n.getIssue().getTrackingNumber();
            }
            return dto;
        }

        public Long getId() { return id; }
        public Long getRecipientId() { return recipientId; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public NotificationType getNotificationType() { return notificationType; }
        public Long getIssueId() { return issueId; }
        public String getIssueTrackingNumber() { return issueTrackingNumber; }
        public boolean isRead() { return isRead; }
        public Instant getCreatedAt() { return createdAt; }
    }
}
