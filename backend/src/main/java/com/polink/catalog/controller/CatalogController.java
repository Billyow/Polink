package com.polink.catalog.controller;

import com.polink.catalog.dto.BranchResponseDto;
import com.polink.catalog.dto.SubjectResponseDto;
import com.polink.catalog.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
@Tag(name = "Catalog (Public)", description = "Consulta pública del catálogo cerrado de Ramas y Materias activas")
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/branches")
    @Operation(summary = "Listar ramas académicas activas", description = "Retorna todas las ramas académicas activas ordenadas alfabéticamente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ramas obtenidas correctamente")
    })
    public ResponseEntity<List<BranchResponseDto>> listBranches() {
        return ResponseEntity.ok(catalogService.listPublicBranches());
    }

    @GetMapping("/subjects")
    @Operation(summary = "Listar asignaturas activas", description = "Retorna las asignaturas activas del catálogo, permitiendo filtrar opcionalmente por rama (branchId).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asignaturas obtenidas correctamente")
    })
    public ResponseEntity<List<SubjectResponseDto>> listSubjects(
            @RequestParam(required = false) UUID branchId
    ) {
        return ResponseEntity.ok(catalogService.listPublicSubjects(branchId));
    }
}
