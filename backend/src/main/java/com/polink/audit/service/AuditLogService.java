package com.polink.audit.service;

import com.polink.audit.dto.AuditLogResponseDto;
import com.polink.audit.entity.AuditLogEntity;
import com.polink.audit.repository.AuditLogRepository;
import com.polink.common.dto.PagedResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void recordAction(
            UUID actorUserId,
            String actorRole,
            String actionType,
            String targetEntity,
            UUID targetId,
            String reasonOrDetails
    ) {
        AuditLogEntity entity = AuditLogEntity.builder()
                .id(UUID.randomUUID())
                .actorUserId(actorUserId)
                .actorRole(actorRole)
                .actionType(actionType)
                .targetEntity(targetEntity)
                .targetId(targetId)
                .reasonOrDetails(reasonOrDetails)
                .createdAt(Instant.now())
                .build();
        auditLogRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public PagedResponseDto<AuditLogResponseDto> listAuditLogs(
            String actionType,
            UUID actorUserId,
            int page,
            int size
    ) {
        String normalizedAction = (actionType == null || actionType.isBlank()) ? null : actionType.trim();
        Page<AuditLogResponseDto> resultPage = auditLogRepository
                .findFiltered(normalizedAction, actorUserId, PagedResponseDto.pageRequest(page, size))
                .map(AuditLogResponseDto::fromEntity);
        return PagedResponseDto.from(resultPage);
    }
}
