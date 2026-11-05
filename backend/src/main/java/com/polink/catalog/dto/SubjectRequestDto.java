package com.polink.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Datos para crear o actualizar una asignatura del catálogo")
public record SubjectRequestDto(
        @NotNull(message = "El identificador de la rama es obligatorio")
        @Schema(description = "UUID de la rama a la que pertenece la asignatura")
        UUID branchId,

        @NotBlank(message = "El código de la asignatura es obligatorio")
        @Size(max = 40, message = "El código no puede superar 40 caracteres")
        @Schema(description = "Código institucional único de la materia", example = "CB-105")
        String code,

        @NotBlank(message = "El nombre de la asignatura es obligatorio")
        @Size(max = 150, message = "El nombre de la asignatura no puede superar 150 caracteres")
        @Schema(description = "Nombre oficial de la asignatura", example = "Ecuaciones Diferenciales")
        String name,

        @Schema(description = "Indica si la asignatura está activa en el catálogo (por defecto true)", example = "true")
        Boolean active
) {
}
