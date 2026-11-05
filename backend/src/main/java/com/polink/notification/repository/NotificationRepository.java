package com.polink.notification.repository;

import com.polink.notification.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    Page<NotificationEntity> findByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId, Pageable pageable);

    long countByRecipientUserIdAndReadFalse(UUID recipientUserId);

    Optional<NotificationEntity> findByIdAndRecipientUserId(UUID id, UUID recipientUserId);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.read = true WHERE n.recipientUserId = :recipientUserId AND n.read = false")
    int markAllAsReadForUser(@Param("recipientUserId") UUID recipientUserId);
}
