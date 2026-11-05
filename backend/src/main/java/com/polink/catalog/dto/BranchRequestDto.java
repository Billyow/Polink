package com.polink.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear o actualizar una rama de conocimiento")
public record BranchRequestDto(
        @NotBlank(message = "El nombre de la rama es obligatorio")
        @Size(max = 120, message = "El nombre de la rama no puede superar 120 caracteres")
        @Schema(description = "Nombre único de la rama", example = "Ciencias Básicas")
        String name,

        @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
        @Schema(description = "Descripción de la rama académica", example = "Asignaturas fundamentales de matemáticas y física.")
        String description,

        @Schema(description = "Indica si la rama está activa en el catálogo (por defecto true)", example = "true")
        Boolean active
) {
}
