package com.polink.user.repository;

import com.polink.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            SELECT u FROM UserEntity u
            WHERE (:status IS NULL OR u.status = :status)
            ORDER BY u.createdAt DESC
            """)
    Page<UserEntity> findFiltered(@Param("status") String status, Pageable pageable);

    @Query("""
            SELECT DISTINCT u FROM UserEntity u
            JOIN u.roles r
            WHERE r.name = :roleName AND u.status = 'ACTIVE'
            """)
    List<UserEntity> findActiveUsersByRoleName(@Param("roleName") String roleName);
}
