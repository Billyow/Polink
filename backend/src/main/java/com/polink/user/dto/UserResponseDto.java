package com.polink.user.dto;

import com.polink.user.entity.RoleEntity;
import com.polink.user.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Schema(description = "Información pública/administrativa de un usuario")
public record UserResponseDto(
        @Schema(description = "Identificador único del usuario")
        UUID id,
        @Schema(description = "Nombre completo del usuario", example = "Laura Gómez")
        String fullName,
        @Schema(description = "Correo electrónico registrado", example = "laura@polink.edu.co")
        String email,
        @Schema(description = "Estado de la cuenta (ACTIVE, SUSPENDED)", example = "ACTIVE")
        String status,
        @Schema(description = "Conjunto de roles asignados a la cuenta")
        Set<String> roles,
        @Schema(description = "Fecha y hora UTC de registro")
        Instant createdAt
) {
    public static UserResponseDto fromEntity(UserEntity entity) {
        Set<String> roleNames = entity.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toUnmodifiableSet());
        return new UserResponseDto(
                entity.getId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getStatus(),
                roleNames,
                entity.getCreatedAt()
        );
    }
}
