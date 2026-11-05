package com.polink.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales de inicio de sesión")
public record LoginRequestDto(
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El formato del correo electrónico no es válido")
        @Schema(description = "Correo electrónico registrado", example = "andres@polink.edu.co")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Schema(description = "Contraseña del usuario", example = "Estudiante2026!")
        String password
) {
}
