package com.polink.user.service;

import com.polink.audit.service.AuditLogService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return UserResponseDto.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public PagedResponseDto<UserResponseDto> listUsers(String status, int page, int size) {
        String normalizedStatus = (status == null || status.isBlank()) ? null : status.trim().toUpperCase();
        Page<UserResponseDto> pageResult = userRepository
                .findFiltered(normalizedStatus, PageRequest.of(page, size))
                .map(UserResponseDto::fromEntity);
        return PagedResponseDto.from(pageResult);
    }

    @Transactional
    public UserResponseDto createSupervisor(UUID adminUserId, CreateSupervisorRequestDto request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("El correo electrónico ya está registrado");
        }
        RoleEntity supervisorRole = roleRepository.findByName("ROLE_SUPERVISOR")
                .orElseThrow(() -> new ResourceNotFoundException("El rol ROLE_SUPERVISOR no existe en el sistema"));

        Instant now = Instant.now();
        UserEntity supervisor = UserEntity.builder()
                .id(UUID.randomUUID())
                .fullName(request.fullName().trim())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .roles(new HashSet<>(Set.of(supervisorRole)))
                .build();

        UserEntity saved = userRepository.save(supervisor);
        auditLogService.recordAction(
                adminUserId,
                "ROLE_ADMIN",
                "CREATE_SUPERVISOR",
                "USER",
                saved.getId(),
                "Creación de cuenta de supervisor para " + saved.getEmail()
        );
        notificationService.notifyUser(
                saved.getId(),
                saved.getEmail(),
                "Cuenta de Supervisor creada",
                "Se ha habilitado tu cuenta como Supervisor en Polink.",
                "SUPERVISOR_CREATED",
                saved.getId()
        );
        return UserResponseDto.fromEntity(saved);
    }

    @Transactional
    public UserResponseDto updateUserStatus(UUID adminUserId, UUID targetUserId, UpdateUserStatusRequestDto request) {
        if (adminUserId.equals(targetUserId)) {
            throw new DomainException("Un administrador no puede modificar el estado de su propia cuenta");
        }
        UserEntity targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario objetivo no encontrado"));

        String newStatus = request.status().trim().toUpperCase();
        targetUser.setStatus(newStatus);
        targetUser.setUpdatedAt(Instant.now());
        UserEntity saved = userRepository.save(targetUser);

        auditLogService.recordAction(
                adminUserId,
                "ROLE_ADMIN",
                "UPDATE_USER_STATUS_" + newStatus,
                "USER",
                saved.getId(),
                request.reason().trim()
        );
        notificationService.notifyUser(
                saved.getId(),
                saved.getEmail(),
                "Actualización de estado de tu cuenta",
                "El estado de tu cuenta ha cambiado a " + newStatus + ". Motivo: " + request.reason().trim(),
                "USER_STATUS_UPDATED",
                saved.getId()
        );
        return UserResponseDto.fromEntity(saved);
    }
}
