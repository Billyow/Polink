package com.polink.audit.dto;

import com.polink.audit.entity.AuditLogEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Registro inmutable de auditoría de una acción administrativa o de supervisión")
public record AuditLogResponseDto(
        @Schema(description = "Identificador único del registro de auditoría")
        UUID id,
        @Schema(description = "Identificador del usuario que ejecutó la acción")
        UUID actorUserId,
        @Schema(description = "Rol del actor al momento de ejecutar la acción", example = "ROLE_SUPERVISOR")
        String actorRole,
        @Schema(description = "Tipo de acción ejecutada", example = "DEACTIVATE_OFFER")
        String actionType,
        @Schema(description = "Entidad objetivo afectada", example = "TUTORING_OFFER")
        String targetEntity,
        @Schema(description = "Identificador de la entidad objetivo")
        UUID targetId,
        @Schema(description = "Justificación o detalles registrados")
        String reasonOrDetails,
        @Schema(description = "Fecha y hora UTC de creación")
        Instant createdAt
) {
    public static AuditLogResponseDto fromEntity(AuditLogEntity entity) {
        return new AuditLogResponseDto(
                entity.getId(),
                entity.getActorUserId(),
                entity.getActorRole(),
                entity.getActionType(),
                entity.getTargetEntity(),
                entity.getTargetId(),
                entity.getReasonOrDetails(),
                entity.getCreatedAt()
        );
    }
}
