package com.polink.notification.controller;

import com.polink.common.config.OpenApiSwaggerConfig;
import com.polink.common.dto.PagedResponseDto;
import com.polink.common.security.SecurityUserPrincipal;
import com.polink.notification.dto.NotificationResponseDto;
import com.polink.notification.dto.UnreadCountResponseDto;
import com.polink.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Gestión de notificaciones In-App y conteo ligero para Polling")
@SecurityRequirement(name = OpenApiSwaggerConfig.BEARER_AUTH_SCHEME)
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Listar notificaciones del usuario autenticado", description = "Retorna las notificaciones In-App del usuario ordenadas de la más reciente a la más antigua.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificaciones obtenidas correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<PagedResponseDto<NotificationResponseDto>> listMyNotifications(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(notificationService.listUserNotifications(principal.getUserId(), page, size));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Obtener conteo de notificaciones no leídas", description = "Endpoint ligero consumido por el polling inteligente del frontend cada 45-60 segundos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conteo obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<UnreadCountResponseDto> getUnreadCount(
            @AuthenticationPrincipal SecurityUserPrincipal principal
    ) {
        return ResponseEntity.ok(notificationService.getUnreadCount(principal.getUserId()));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marcar una notificación como leída")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificación marcada como leída"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    })
    public ResponseEntity<NotificationResponseDto> markAsRead(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(notificationService.markAsRead(principal.getUserId(), id));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Marcar todas las notificaciones del usuario como leídas")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Todas las notificaciones fueron marcadas como leídas")
    })
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal SecurityUserPrincipal principal
    ) {
        notificationService.markAllAsRead(principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
