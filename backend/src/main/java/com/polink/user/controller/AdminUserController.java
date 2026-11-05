package com.polink.user.controller;

import com.polink.common.config.OpenApiSwaggerConfig;
import com.polink.common.dto.PagedResponseDto;
import com.polink.common.security.SecurityUserPrincipal;
import com.polink.user.dto.CreateSupervisorRequestDto;
import com.polink.user.dto.UpdateUserStatusRequestDto;
import com.polink.user.dto.UserResponseDto;
import com.polink.user.service.UserService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin - Users & Supervisors", description = "Gestión administrativa de cuentas de usuarios y creación de supervisores")
@SecurityRequirement(name = OpenApiSwaggerConfig.BEARER_AUTH_SCHEME)
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/users")
    @Operation(summary = "Listar usuarios registrados", description = "Retorna de forma paginada las cuentas registradas, con filtro opcional por estado (ACTIVE o SUSPENDED).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuarios obtenidos correctamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: requiere ROLE_ADMIN")
    })
    public ResponseEntity<PagedResponseDto<UserResponseDto>> listUsers(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(userService.listUsers(status, page, size));
    }

    @PostMapping("/supervisors")
    @Operation(summary = "Crear cuenta de Supervisor", description = "Registra un nuevo usuario con rol ROLE_SUPERVISOR y deja constancia en el log de auditoría.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Supervisor creado exitosamente"),
            @ApiResponse(responseCode = "409", description = "El correo electrónico ya está registrado")
    })
    public ResponseEntity<UserResponseDto> createSupervisor(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @Valid @RequestBody CreateSupervisorRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createSupervisor(principal.getUserId(), request));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Activar o suspender usuario", description = "Modifica el estado de una cuenta entre ACTIVE y SUSPENDED registrando el motivo obligatorio en audit_logs.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado del usuario actualizado"),
            @ApiResponse(responseCode = "400", description = "Intento de auto-suspensión o datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UserResponseDto> updateUserStatus(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserStatusRequestDto request
    ) {
        return ResponseEntity.ok(userService.updateUserStatus(principal.getUserId(), id, request));
    }
}
