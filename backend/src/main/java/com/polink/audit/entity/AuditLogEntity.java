package com.polink.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "actor_user_id", nullable = false, updatable = false)
    private UUID actorUserId;

    @Column(name = "actor_role", nullable = false, length = 50, updatable = false)
    private String actorRole;

    @Column(name = "action_type", nullable = false, length = 80, updatable = false)
    private String actionType;

    @Column(name = "target_entity", nullable = false, length = 80, updatable = false)
    private String targetEntity;

    @Column(name = "target_id", updatable = false)
    private UUID targetId;

    @Column(name = "reason_or_details", columnDefinition = "TEXT", updatable = false)
    private String reasonOrDetails;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
