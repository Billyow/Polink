package com.polink.audit.service;

import com.polink.audit.dto.AuditLogResponseDto;
import com.polink.audit.entity.AuditLogEntity;
import com.polink.audit.repository.AuditLogRepository;
import com.polink.common.dto.PagedResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void recordActionShouldPersistAuditLogEntityWithTimestampAndNewId() {
        UUID actorId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        auditLogService.recordAction(
                actorId,
                "ROLE_SUPERVISOR",
                "DEACTIVATE_OFFER",
                "TUTORING_OFFER",
                targetId,
                "Temario no coincide con el curso oficial"
        );

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLogEntity saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getActorUserId()).isEqualTo(actorId);
        assertThat(saved.getActorRole()).isEqualTo("ROLE_SUPERVISOR");
        assertThat(saved.getActionType()).isEqualTo("DEACTIVATE_OFFER");
        assertThat(saved.getTargetEntity()).isEqualTo("TUTORING_OFFER");
        assertThat(saved.getTargetId()).isEqualTo(targetId);
        assertThat(saved.getReasonOrDetails()).isEqualTo("Temario no coincide con el curso oficial");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void listAuditLogsShouldNormalizeActionFilterAndReturnPagedResponse() {
        UUID actorId = UUID.randomUUID();
        AuditLogEntity entity = AuditLogEntity.builder()
                .id(UUID.randomUUID())
                .actorUserId(actorId)
                .actorRole("ROLE_ADMIN")
                .actionType("CREATE_SUBJECT")
                .targetEntity("SUBJECT")
                .targetId(UUID.randomUUID())
                .reasonOrDetails("Creación de Cálculo Vectorial")
                .createdAt(Instant.now())
                .build();

        when(auditLogRepository.findFiltered(eq("CREATE_SUBJECT"), eq(actorId), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1));

        PagedResponseDto<AuditLogResponseDto> result = auditLogService.listAuditLogs("  CREATE_SUBJECT  ", actorId, 0, 10);

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).actionType()).isEqualTo("CREATE_SUBJECT");
        assertThat(result.content().get(0).actorRole()).isEqualTo("ROLE_ADMIN");
    }
}
