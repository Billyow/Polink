package com.polink.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos requeridos para que un administrador cree una cuenta de Supervisor")
public record CreateSupervisorRequestDto(
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        @Schema(description = "Nombre completo del supervisor", example = "Carlos Ruiz")
        String fullName,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "Debe proporcionar un correo electrónico válido")
        @Schema(description = "Correo electrónico institucional del supervisor", example = "carlos.supervisor@polink.edu.co")
        String email,

        @NotBlank(message = "La contraseña inicial es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "La contraseña debe incluir al menos una letra mayúscula, una minúscula y un número"
        )
        @Schema(description = "Contraseña inicial de acceso (mínimo 8 caracteres, mayúscula, minúscula y número)", example = "Supervisor2026!")
        String password
) {
}
