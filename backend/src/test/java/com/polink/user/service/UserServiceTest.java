package com.polink.user.service;

import com.polink.audit.service.AuditLogService;
import com.polink.auth.repository.PasswordResetTokenRepository;
import com.polink.common.dto.PagedResponseDto;
import com.polink.common.exception.ConflictException;
import com.polink.common.exception.DomainException;
import com.polink.common.exception.ResourceNotFoundException;
import com.polink.notification.service.NotificationService;
import com.polink.user.dto.CreateSupervisorRequestDto;
import com.polink.user.dto.UpdateUserStatusRequestDto;
import com.polink.user.dto.UserResponseDto;
import com.polink.user.entity.RoleEntity;
import com.polink.user.entity.UserEntity;
import com.polink.user.repository.RoleRepository;
import com.polink.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserService userService;

    @Test
    void getCurrentUserAndListUsersShouldValidateParametersAndReturnDtos() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .fullName("Laura Gómez")
                .email("laura@polink.edu.co")
                .passwordHash("hash")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .roles(Set.of(new RoleEntity(UUID.randomUUID(), "ROLE_STUDENT")))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        assertThat(userService.getCurrentUser(userId).email()).isEqualTo("laura@polink.edu.co");

        UUID missingId = UUID.randomUUID();
        when(userRepository.findById(missingId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getCurrentUser(missingId))
                .isInstanceOf(ResourceNotFoundException.class);

        when(userRepository.findFiltered(eq("ACTIVE"), eq(PageRequest.of(0, 10))))
                .thenReturn(new PageImpl<>(List.of(user)));
        PagedResponseDto<UserResponseDto> page = userService.listUsers("active", 0, 10);
        assertThat(page.content()).hasSize(1);

        assertThatThrownBy(() -> userService.listUsers("INVALID", 0, 10))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> userService.listUsers("ACTIVE", -1, 10))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> userService.listUsers("ACTIVE", 0, 200))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void createSupervisorShouldSaveUserWithSupervisorRoleAndRecordAudit() {
        UUID adminId = UUID.randomUUID();
        RoleEntity supervisorRole = new RoleEntity(UUID.randomUUID(), "ROLE_SUPERVISOR");
        when(userRepository.existsByEmailIgnoreCase("supervisor@polink.edu.co")).thenReturn(false);
        when(roleRepository.findByName("ROLE_SUPERVISOR")).thenReturn(Optional.of(supervisorRole));
        when(passwordEncoder.encode("Supervisor2026!")).thenReturn("hashed-pwd");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDto result = userService.createSupervisor(
                adminId,
                new CreateSupervisorRequestDto("Carlos Ruiz", "supervisor@polink.edu.co", "Supervisor2026!")
        );

        assertThat(result.email()).isEqualTo("supervisor@polink.edu.co");
        assertThat(result.roles()).containsExactly("ROLE_SUPERVISOR");
        verify(auditLogService).recordAction(
                eq(adminId),
                eq("ROLE_ADMIN"),
                eq("CREATE_SUPERVISOR"),
                eq("USER"),
                eq(result.id()),
                any()
        );
    }

    @Test
    void createSupervisorShouldThrowConflictWhenEmailAlreadyExists() {
        UUID adminId = UUID.randomUUID();
        when(userRepository.existsByEmailIgnoreCase("dup@polink.edu.co")).thenReturn(true);

        assertThatThrownBy(() -> userService.createSupervisor(
                adminId,
                new CreateSupervisorRequestDto("Dup", "dup@polink.edu.co", "Supervisor2026!")
        )).isInstanceOf(ConflictException.class);
    }

    @Test
    void updateUserStatusShouldPreventSelfOrAdminSuspensionAndInvalidateTokensOnSuspend() {
        UUID adminId = UUID.randomUUID();
        UpdateUserStatusRequestDto request = new UpdateUserStatusRequestDto("SUSPENDED", "Abuso reportado");

        assertThatThrownBy(() -> userService.updateUserStatus(adminId, adminId, request))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("propia cuenta");

        UUID otherAdminId = UUID.randomUUID();
        UserEntity otherAdmin = UserEntity.builder()
                .id(otherAdminId)
                .fullName("Otro Admin")
                .email("admin2@polink.edu.co")
                .passwordHash("hash")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .roles(Set.of(new RoleEntity(UUID.randomUUID(), "ROLE_ADMIN")))
                .build();
        when(userRepository.findById(otherAdminId)).thenReturn(Optional.of(otherAdmin));
        assertThatThrownBy(() -> userService.updateUserStatus(adminId, otherAdminId, request))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("ROLE_ADMIN");

        UUID targetId = UUID.randomUUID();
        UserEntity targetUser = UserEntity.builder()
                .id(targetId)
                .fullName("Estudiante Uno")
                .email("est@polink.edu.co")
                .passwordHash("hash")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .roles(Set.of(new RoleEntity(UUID.randomUUID(), "ROLE_STUDENT")))
                .build();

        when(userRepository.findById(targetId)).thenReturn(Optional.of(targetUser));
        when(userRepository.save(targetUser)).thenReturn(targetUser);

        UserResponseDto updated = userService.updateUserStatus(adminId, targetId, request);

        assertThat(updated.status()).isEqualTo("SUSPENDED");
        verify(passwordResetTokenRepository).invalidateAllActiveTokensForUser(targetId);
        verify(auditLogService).recordAction(
                adminId,
                "ROLE_ADMIN",
                "UPDATE_USER_STATUS_SUSPENDED",
                "USER",
                targetId,
                "Abuso reportado"
        );
    }
}
