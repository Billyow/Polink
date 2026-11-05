package com.polink.auth.dto;

import com.polink.user.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de autenticación con token JWT y perfil del usuario")
public record AuthResponseDto(
        @Schema(description = "Token JWT firmado")
        String accessToken,
        @Schema(description = "Tipo de esquema de autenticación", example = "Bearer")
        String tokenType,
        @Schema(description = "Tiempo de expiración en milisegundos", example = "86400000")
        long expiresInMs,
        @Schema(description = "Información del usuario autenticado")
        UserResponseDto user
) {
}
