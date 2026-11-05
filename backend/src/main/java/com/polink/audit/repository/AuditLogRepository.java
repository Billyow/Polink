package com.polink.audit.repository;

import com.polink.audit.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {

    @Query("""
            SELECT a FROM AuditLogEntity a
            WHERE (:actionType IS NULL OR a.actionType = :actionType)
              AND (:actorUserId IS NULL OR a.actorUserId = :actorUserId)
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLogEntity> findFiltered(
            @Param("actionType") String actionType,
            @Param("actorUserId") UUID actorUserId,
            Pageable pageable
    );
}
