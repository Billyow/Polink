package com.polink.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitud de restablecimiento de contraseña mediante token")
public record ResetPasswordRequestDto(
        @NotBlank(message = "El token de recuperación es obligatorio")
        @Schema(description = "Token temporal enviado al correo del usuario")
        String token,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La nueva contraseña debe tener entre 8 y 100 caracteres")
        @Schema(description = "Nueva contraseña de la cuenta (mínimo 8 caracteres)", example = "NuevaClave2026!")
        String newPassword
) {
}
