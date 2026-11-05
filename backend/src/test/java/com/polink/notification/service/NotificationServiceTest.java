package com.polink.notification.service;

import com.polink.common.dto.PagedResponseDto;
import com.polink.common.exception.ResourceNotFoundException;
import com.polink.notification.dto.NotificationResponseDto;
import com.polink.notification.dto.UnreadCountResponseDto;
import com.polink.notification.entity.NotificationEntity;
import com.polink.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailNotificationSender emailNotificationSender;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void notifyUserShouldSaveNotificationAndTriggerAsyncEmail() {
        UUID userId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();
        when(notificationRepository.save(any(NotificationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponseDto dto = notificationService.notifyUser(
                userId,
                "asesor@polink.edu.co",
                "Oferta aprobada",
                "Tu oferta de Álgebra Lineal fue aprobada.",
                "OFFER_APPROVED",
                referenceId
        );

        assertThat(dto.id()).isNotNull();
        assertThat(dto.title()).isEqualTo("Oferta aprobada");
        assertThat(dto.read()).isFalse();
        assertThat(dto.referenceId()).isEqualTo(referenceId);
        verify(emailNotificationSender).sendEmailAsync(
                "asesor@polink.edu.co",
                "[Polink] Oferta aprobada",
                "Tu oferta de Álgebra Lineal fue aprobada."
        );
    }

    @Test
    void listUserNotificationsShouldReturnPagedItems() {
        UUID userId = UUID.randomUUID();
        NotificationEntity entity = NotificationEntity.builder()
                .id(UUID.randomUUID())
                .recipientUserId(userId)
                .title("Nueva solicitud")
                .message("Tienes una solicitud pendiente")
                .type("BOOKING_REQUESTED")
                .read(false)
                .createdAt(Instant.now())
                .build();

        when(notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(eq(userId), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1));

        PagedResponseDto<NotificationResponseDto> response = notificationService.listUserNotifications(userId, 0, 20);

        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.content().get(0).type()).isEqualTo("BOOKING_REQUESTED");
    }

    @Test
    void getUnreadCountShouldReturnRepositoryCount() {
        UUID userId = UUID.randomUUID();
        when(notificationRepository.countByRecipientUserIdAndReadFalse(userId)).thenReturn(4L);

        UnreadCountResponseDto response = notificationService.getUnreadCount(userId);

        assertThat(response.unreadCount()).isEqualTo(4L);
    }

    @Test
    void markAsReadShouldUpdateEntityOrThrowNotFound() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        NotificationEntity entity = NotificationEntity.builder()
                .id(notificationId)
                .recipientUserId(userId)
                .title("Aviso")
                .message("Detalle")
                .type("INFO")
                .read(false)
                .createdAt(Instant.now())
                .build();

        when(notificationRepository.findByIdAndRecipientUserId(notificationId, userId))
                .thenReturn(Optional.of(entity));
        when(notificationRepository.save(entity)).thenReturn(entity);

        NotificationResponseDto updated = notificationService.markAsRead(userId, notificationId);
        assertThat(updated.read()).isTrue();

        UUID missingId = UUID.randomUUID();
        when(notificationRepository.findByIdAndRecipientUserId(missingId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(userId, missingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void markAllAsReadShouldDelegateToRepository() {
        UUID userId = UUID.randomUUID();
        notificationService.markAllAsRead(userId);
        verify(notificationRepository).markAllAsReadForUser(userId);
    }
}
