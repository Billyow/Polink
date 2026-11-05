package com.polink.auth.service;

import com.polink.auth.dto.AuthResponseDto;
import com.polink.auth.dto.ForgotPasswordRequestDto;
import com.polink.auth.dto.LoginRequestDto;
import com.polink.auth.dto.MessageResponseDto;
import com.polink.auth.dto.ResetPasswordRequestDto;
import com.polink.auth.dto.StudentRegisterRequestDto;
import com.polink.auth.entity.PasswordResetTokenEntity;
import com.polink.auth.repository.PasswordResetTokenRepository;
import com.polink.common.exception.ConflictException;
import com.polink.common.exception.DomainException;
import com.polink.common.exception.ResourceNotFoundException;
import com.polink.common.security.JwtService;
import com.polink.notification.service.NotificationService;
import com.polink.user.dto.UserResponseDto;
import com.polink.user.entity.RoleEntity;
import com.polink.user.entity.UserEntity;
import com.polink.user.repository.RoleRepository;
import com.polink.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationService notificationService;

    @Value("${app.security.password-reset-expiration-minutes:30}")
    private long resetExpirationMinutes;

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Transactional
    public AuthResponseDto registerStudent(StudentRegisterRequestDto request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("El correo electrónico ya está registrado");
        }

        RoleEntity studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseThrow(() -> new ResourceNotFoundException("El rol ROLE_STUDENT no existe en el sistema"));

        Instant now = Instant.now();
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .fullName(request.fullName().trim())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .roles(new HashSet<>(Set.of(studentRole)))
                .build();

        UserEntity saved = userRepository.save(user);
        notificationService.notifyUser(
                saved.getId(),
                saved.getEmail(),
                "Bienvenido a Polink",
                "Tu cuenta de estudiante ha sido creada exitosamente.",
                "STUDENT_REGISTERED",
                saved.getId()
        );

        return buildAuthResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new DomainException("Credenciales inválidas", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new DomainException("Credenciales inválidas", HttpStatus.UNAUTHORIZED);
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new DomainException("La cuenta se encuentra suspendida", HttpStatus.FORBIDDEN);
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public MessageResponseDto forgotPassword(ForgotPasswordRequestDto request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        Optional<UserEntity> userOpt = userRepository.findByEmailIgnoreCase(normalizedEmail);

        if (userOpt.isPresent() && "ACTIVE".equalsIgnoreCase(userOpt.get().getStatus())) {
            UserEntity user = userOpt.get();
            passwordResetTokenRepository.invalidateAllActiveTokensForUser(user.getId());

            String rawToken = UUID.randomUUID().toString().replace("-", "")
                    + UUID.randomUUID().toString().replace("-", "");
            Instant now = Instant.now();
            PasswordResetTokenEntity tokenEntity = PasswordResetTokenEntity.builder()
                    .id(UUID.randomUUID())
                    .userId(user.getId())
                    .token(rawToken)
                    .expiresAt(now.plus(Duration.ofMinutes(resetExpirationMinutes)))
                    .used(false)
                    .createdAt(now)
                    .build();
            passwordResetTokenRepository.save(tokenEntity);

            String resetUrl = frontendBaseUrl + "/reset-password?token=" + rawToken;
            notificationService.notifyUser(
                    user.getId(),
                    user.getEmail(),
                    "Recuperación de contraseña",
                    "Has solicitado restablecer tu contraseña. Usa el siguiente enlace o token (válido por "
                            + resetExpirationMinutes + " minutos): " + resetUrl,
                    "PASSWORD_RESET_REQUESTED",
                    tokenEntity.getId()
            );
        }

        return new MessageResponseDto(
                "Si el correo se encuentra registrado, recibirás instrucciones para restablecer tu contraseña"
        );
    }

    @Transactional
    public MessageResponseDto resetPassword(ResetPasswordRequestDto request) {
        PasswordResetTokenEntity tokenEntity = passwordResetTokenRepository
                .findByTokenAndUsedFalse(request.token().trim())
                .orElseThrow(() -> new DomainException("Token de recuperación inválido o ya utilizado"));

        if (tokenEntity.getExpiresAt().isBefore(Instant.now())) {
            tokenEntity.setUsed(true);
            passwordResetTokenRepository.save(tokenEntity);
            throw new DomainException("El token de recuperación ha expirado");
        }

        UserEntity user = userRepository.findById(tokenEntity.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario asociado al token no encontrado"));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        tokenEntity.setUsed(true);
        passwordResetTokenRepository.save(tokenEntity);

        notificationService.notifyUser(
                user.getId(),
                user.getEmail(),
                "Contraseña actualizada",
                "Tu contraseña ha sido restablecida exitosamente.",
                "PASSWORD_RESET_COMPLETED",
                user.getId()
        );

        return new MessageResponseDto("Tu contraseña ha sido restablecida exitosamente");
    }

    private AuthResponseDto buildAuthResponse(UserEntity user) {
        UserResponseDto userDto = UserResponseDto.fromEntity(user);
        String token = jwtService.generateToken(user.getId(), user.getEmail(), userDto.roles());
        return new AuthResponseDto(token, "Bearer", jwtService.getExpirationMs(), userDto);
    }
}
