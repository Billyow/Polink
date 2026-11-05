package com.polink.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta ligera para el polling de notificaciones no leídas")
public record UnreadCountResponseDto(
        @Schema(description = "Cantidad total de notificaciones sin leer del usuario autenticado", example = "3")
        long unreadCount
) {
}
