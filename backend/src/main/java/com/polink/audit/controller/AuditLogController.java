package com.polink.audit.controller;

import com.polink.audit.dto.AuditLogResponseDto;
import com.polink.audit.service.AuditLogService;
import com.polink.common.config.OpenApiSwaggerConfig;
import com.polink.common.dto.PagedResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Admin - Audit Logs", description = "Consulta de registros inmutables de auditoría para administradores")
@SecurityRequirement(name = OpenApiSwaggerConfig.BEARER_AUTH_SCHEME)
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Listar registros de auditoría", description = "Retorna de forma paginada el historial inmutable de acciones realizadas por Administradores y Supervisores.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de auditoría obtenido correctamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: requiere ROLE_ADMIN")
    })
    public ResponseEntity<PagedResponseDto<AuditLogResponseDto>> listAuditLogs(
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) UUID actorUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(auditLogService.listAuditLogs(actionType, actorUserId, page, size));
    }
}
