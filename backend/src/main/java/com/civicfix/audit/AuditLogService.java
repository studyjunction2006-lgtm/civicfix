package com.civicfix.audit;

import com.civicfix.common.PagedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String action, String entityType, String entityId, Long actorId, String actorEmail, String actorRole, String details) {
        try {
            AuditLog auditLog = new AuditLog(action, entityType, entityId, actorId, actorEmail, actorRole, details, null);
            auditLogRepository.save(auditLog);
        } catch (Exception ex) {
            log.warn("Failed to write audit log record: {}", ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogDto> getAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> paged = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<AuditLogDto> dtoList = paged.getContent().stream().map(AuditLogDto::fromEntity).toList();
        return new PagedResponse<>(dtoList, paged.getNumber(), paged.getSize(), paged.getTotalElements(), paged.getTotalPages(), paged.isLast());
    }

    public static class AuditLogDto {
        private Long id;
        private String action;
        private String entityType;
        private String entityId;
        private Long actorId;
        private String actorEmail;
        private String actorRole;
        private String details;
        private Instant createdAt;

        public static AuditLogDto fromEntity(AuditLog log) {
            AuditLogDto dto = new AuditLogDto();
            dto.id = log.getId();
            dto.action = log.getAction();
            dto.entityType = log.getEntityType();
            dto.entityId = log.getEntityId();
            dto.actorId = log.getActorId();
            dto.actorEmail = log.getActorEmail();
            dto.actorRole = log.getActorRole();
            dto.details = log.getDetails();
            dto.createdAt = log.getCreatedAt();
            return dto;
        }

        public Long getId() { return id; }
        public String getAction() { return action; }
        public String getEntityType() { return entityType; }
        public String getEntityId() { return entityId; }
        public Long getActorId() { return actorId; }
        public String getActorEmail() { return actorEmail; }
        public String getActorRole() { return actorRole; }
        public String getDetails() { return details; }
        public Instant getCreatedAt() { return createdAt; }
    }
}
