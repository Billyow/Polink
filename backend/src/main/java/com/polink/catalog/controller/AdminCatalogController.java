package com.polink.catalog.controller;

import com.polink.catalog.dto.BranchRequestDto;
import com.polink.catalog.dto.BranchResponseDto;
import com.polink.catalog.dto.SubjectRequestDto;
import com.polink.catalog.dto.SubjectResponseDto;
import com.polink.catalog.service.CatalogService;
import com.polink.common.config.OpenApiSwaggerConfig;
import com.polink.common.security.SecurityUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/catalog")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin - Catalog", description = "Gestión administrativa del catálogo cerrado de Ramas y Materias")
@SecurityRequirement(name = OpenApiSwaggerConfig.BEARER_AUTH_SCHEME)
public class AdminCatalogController {

    private final CatalogService catalogService;

    @PostMapping("/branches")
    @Operation(summary = "Crear rama académica", description = "Crea una nueva rama en el catálogo y registra la acción en audit_logs.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rama creada exitosamente"),
            @ApiResponse(responseCode = "409", description = "Ya existe una rama con ese nombre")
    })
    public ResponseEntity<BranchResponseDto> createBranch(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @Valid @RequestBody BranchRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogService.createBranch(principal.getUserId(), request));
    }

    @PutMapping("/branches/{id}")
    @Operation(summary = "Actualizar rama académica", description = "Actualiza el nombre, descripción o estado activo de una rama existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rama actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Rama no encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflicto de nombre con otra rama")
    })
    public ResponseEntity<BranchResponseDto> updateBranch(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody BranchRequestDto request
    ) {
        return ResponseEntity.ok(catalogService.updateBranch(principal.getUserId(), id, request));
    }

    @PostMapping("/subjects")
    @Operation(summary = "Crear asignatura", description = "Crea una nueva asignatura asociada a una rama académica existente y registra la acción en audit_logs.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Asignatura creada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Rama académica no encontrada"),
            @ApiResponse(responseCode = "409", description = "Ya existe una asignatura con ese código")
    })
    public ResponseEntity<SubjectResponseDto> createSubject(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @Valid @RequestBody SubjectRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogService.createSubject(principal.getUserId(), request));
    }

    @PutMapping("/subjects/{id}")
    @Operation(summary = "Actualizar asignatura", description = "Actualiza la rama, código, nombre o estado activo de una asignatura existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asignatura actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Asignatura o rama no encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflicto de código con otra asignatura")
    })
    public ResponseEntity<SubjectResponseDto> updateSubject(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody SubjectRequestDto request
    ) {
        return ResponseEntity.ok(catalogService.updateSubject(principal.getUserId(), id, request));
    }
}
