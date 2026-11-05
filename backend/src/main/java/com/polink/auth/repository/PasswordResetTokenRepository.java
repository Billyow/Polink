package com.polink.auth.repository;

import com.polink.auth.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    Optional<PasswordResetTokenEntity> findByTokenAndUsedFalse(String token);

    @Modifying
    @Query("UPDATE PasswordResetTokenEntity t SET t.used = true WHERE t.userId = :userId AND t.used = false")
    int invalidateAllActiveTokensForUser(@Param("userId") UUID userId);
}
