package com.polink.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Solicitud de recuperación de contraseña por correo")
public record ForgotPasswordRequestDto(
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico no tiene un formato válido")
        @Schema(description = "Correo electrónico registrado de la cuenta", example = "andres@polink.edu.co")
        String email
) {
}
