package com.polink.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitud de cambio de estado de un usuario con motivo de auditoría")
public record UpdateUserStatusRequestDto(
        @NotBlank(message = "El estado es obligatorio")
        @Pattern(regexp = "ACTIVE|SUSPENDED", message = "El estado debe ser ACTIVE o SUSPENDED")
        @Schema(description = "Nuevo estado del usuario", example = "SUSPENDED")
        String status,

        @NotBlank(message = "El motivo del cambio de estado es obligatorio")
        @Size(max = 500, message = "El motivo no puede superar 500 caracteres")
        @Schema(description = "Motivo registrado en el log de auditoría", example = "Incumplimiento reiterado de las normas de la plataforma")
        String reason
) {
}
