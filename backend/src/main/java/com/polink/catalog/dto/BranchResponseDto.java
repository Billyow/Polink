package com.polink.catalog.dto;

import com.polink.catalog.entity.SubjectBranchEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Información de una rama académica del catálogo")
public record BranchResponseDto(
        @Schema(description = "Identificador único de la rama")
        UUID id,
        @Schema(description = "Nombre de la rama", example = "Ciencias Básicas")
        String name,
        @Schema(description = "Descripción de la rama")
        String description,
        @Schema(description = "Estado de activación de la rama")
        boolean active,
        @Schema(description = "Fecha y hora UTC de creación")
        Instant createdAt
) {
    public static BranchResponseDto fromEntity(SubjectBranchEntity entity) {
        return new BranchResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }
}
