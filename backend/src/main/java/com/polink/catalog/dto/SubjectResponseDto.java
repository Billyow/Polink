package com.polink.catalog.dto;

import com.polink.catalog.entity.SubjectEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Información de una asignatura del catálogo")
public record SubjectResponseDto(
        @Schema(description = "Identificador único de la asignatura")
        UUID id,
        @Schema(description = "Identificador de la rama académica")
        UUID branchId,
        @Schema(description = "Nombre de la rama académica", example = "Ciencias Básicas")
        String branchName,
        @Schema(description = "Código institucional único", example = "CB-101")
        String code,
        @Schema(description = "Nombre de la asignatura", example = "Cálculo Diferencial")
        String name,
        @Schema(description = "Estado de activación")
        boolean active,
        @Schema(description = "Fecha y hora UTC de creación")
        Instant createdAt
) {
    public static SubjectResponseDto fromEntity(SubjectEntity entity) {
        return new SubjectResponseDto(
                entity.getId(),
                entity.getBranch().getId(),
                entity.getBranch().getName(),
                entity.getCode(),
                entity.getName(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }
}
