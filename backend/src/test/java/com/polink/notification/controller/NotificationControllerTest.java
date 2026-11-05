package com.polink.notification.controller;

import com.polink.common.config.SecurityConfig;
import com.polink.common.dto.PagedResponseDto;
import com.polink.common.exception.GlobalExceptionHandler;
import com.polink.common.security.SecurityUserPrincipal;
import com.polink.notification.dto.NotificationResponseDto;
import com.polink.notification.dto.UnreadCountResponseDto;
import com.polink.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    void listAndUnreadCountAndMarkReadEndpointsShouldWorkForAuthenticatedPrincipal() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        SecurityUserPrincipal principal = new SecurityUserPrincipal(
                userId,
                "estudiante@polink.edu.co",
                "hashed",
                true,
                Set.of("ROLE_STUDENT")
        );

        NotificationResponseDto item = new NotificationResponseDto(
                notificationId,
                "Reserva confirmada",
                "El asesor aceptó tu solicitud.",
                "BOOKING_ACCEPTED",
                UUID.randomUUID(),
                false,
                Instant.now()
        );

        when(notificationService.listUserNotifications(userId, 0, 20))
                .thenReturn(new PagedResponseDto<>(List.of(item), 0, 20, 1, 1));
        when(notificationService.getUnreadCount(userId))
                .thenReturn(new UnreadCountResponseDto(2L));
        when(notificationService.markAsRead(userId, notificationId))
                .thenReturn(new NotificationResponseDto(
                        notificationId,
                        item.title(),
                        item.message(),
                        item.type(),
                        item.referenceId(),
                        true,
                        item.createdAt()
                ));

        mockMvc.perform(get("/api/v1/notifications").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Reserva confirmada"));

        mockMvc.perform(get("/api/v1/notifications/unread-count").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(2));

        mockMvc.perform(patch("/api/v1/notifications/{id}/read", notificationId).with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));

        mockMvc.perform(patch("/api/v1/notifications/read-all").with(user(principal)))
                .andExpect(status().isNoContent());

        verify(notificationService).markAllAsRead(userId);
    }
}
