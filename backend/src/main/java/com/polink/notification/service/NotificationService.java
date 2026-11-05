package com.polink.notification.service;

import com.polink.common.dto.PagedResponseDto;
import com.polink.common.exception.ResourceNotFoundException;
import com.polink.notification.dto.NotificationResponseDto;
import com.polink.notification.dto.UnreadCountResponseDto;
import com.polink.notification.entity.NotificationEntity;
import com.polink.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailNotificationSender emailNotificationSender;

    @Transactional
    public NotificationResponseDto notifyUser(
            UUID recipientUserId,
            String recipientEmail,
            String title,
            String message,
            String type,
            UUID referenceId
    ) {
        return notifyUserWithSeparateEmailBody(
                recipientUserId,
                recipientEmail,
                title,
                message,
                message,
                type,
                referenceId
        );
    }

    @Transactional
    public NotificationResponseDto notifyUserWithSeparateEmailBody(
            UUID recipientUserId,
            String recipientEmail,
            String title,
            String inAppMessage,
            String emailBody,
            String type,
            UUID referenceId
    ) {
        NotificationEntity entity = NotificationEntity.builder()
                .id(UUID.randomUUID())
                .recipientUserId(recipientUserId)
                .title(title)
                .message(inAppMessage)
                .type(type)
                .referenceId(referenceId)
                .read(false)
                .createdAt(Instant.now())
                .build();
        NotificationEntity saved = notificationRepository.save(entity);
        emailNotificationSender.sendEmailAsync(recipientEmail, "[Polink] " + title, emailBody);
        return NotificationResponseDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponseDto<NotificationResponseDto> listUserNotifications(UUID userId, int page, int size) {
        Page<NotificationResponseDto> resultPage = notificationRepository
                .findByRecipientUserIdOrderByCreatedAtDesc(userId, PagedResponseDto.pageRequest(page, size))
                .map(NotificationResponseDto::fromEntity);
        return PagedResponseDto.from(resultPage);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponseDto getUnreadCount(UUID userId) {
        long count = notificationRepository.countByRecipientUserIdAndReadFalse(userId);
        return new UnreadCountResponseDto(count);
    }

    @Transactional
    public NotificationResponseDto markAsRead(UUID userId, UUID notificationId) {
        NotificationEntity entity = notificationRepository
                .findByIdAndRecipientUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada"));
        entity.setRead(true);
        return NotificationResponseDto.fromEntity(notificationRepository.save(entity));
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadForUser(userId);
    }
}
