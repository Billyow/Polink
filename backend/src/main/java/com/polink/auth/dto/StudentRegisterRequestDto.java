package com.polink.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para el registro de un estudiante")
public record StudentRegisterRequestDto(
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 150, message = "El nombre completo no puede superar 150 caracteres")
        @Schema(description = "Nombre completo del estudiante", example = "Andrés Pérez")
        String fullName,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "Debe ingresar un correo electrónico válido")
        @Schema(description = "Correo electrónico del estudiante", example = "andres@polink.edu.co")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "La contraseña debe incluir al menos una letra mayúscula, una minúscula y un número"
        )
        @Schema(description = "Contraseña de acceso (mínimo 8 caracteres, mayúscula, minúscula y número)", example = "Estudiante2026!")
        String password
) {
}
