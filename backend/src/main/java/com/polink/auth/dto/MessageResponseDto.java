package com.polink.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con mensaje informativo")
public record MessageResponseDto(
        @Schema(description = "Mensaje de confirmación")
        String message
) {
}
