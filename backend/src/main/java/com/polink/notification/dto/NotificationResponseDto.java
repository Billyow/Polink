package com.polink.notification.dto;

import com.polink.notification.entity.NotificationEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Datos de una notificación In-App")
public record NotificationResponseDto(
        @Schema(description = "Identificador único de la notificación")
        UUID id,
        @Schema(description = "Título breve de la notificación")
        String title,
        @Schema(description = "Contenido detallado de la notificación")
        String message,
        @Schema(description = "Categoría o evento que disparó la notificación", example = "OFFER_APPROVED")
        String type,
        @Schema(description = "Identificador del recurso relacionado (reserva, oferta, solicitud)")
        UUID referenceId,
        @Schema(description = "Indica si ya fue leída por el destinatario")
        boolean read,
        @Schema(description = "Fecha y hora UTC de creación")
        Instant createdAt
) {
    public static NotificationResponseDto fromEntity(NotificationEntity entity) {
        return new NotificationResponseDto(
                entity.getId(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getType(),
                entity.getReferenceId(),
                entity.isRead(),
                entity.getCreatedAt()
        );
    }
}
