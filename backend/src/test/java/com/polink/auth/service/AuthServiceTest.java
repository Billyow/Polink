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
import com.polink.common.security.JwtService;
import com.polink.notification.service.NotificationService;
import com.polink.user.entity.RoleEntity;
import com.polink.user.entity.UserEntity;
import com.polink.user.repository.RoleRepository;
import com.polink.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
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
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "resetExpirationMinutes", 30L);
        ReflectionTestUtils.setField(authService, "frontendBaseUrl", "http://localhost:5173");
    }

    @Test
    void registerStudentShouldCreateActiveUserAndReturnJwt() {
        RoleEntity studentRole = new RoleEntity(UUID.randomUUID(), "ROLE_STUDENT");
        when(userRepository.existsByEmailIgnoreCase("andres@polink.edu.co")).thenReturn(false);
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode("Estudiante2026!")).thenReturn("hashed");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(), eq("andres@polink.edu.co"), eq(Set.of("ROLE_STUDENT"))))
                .thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        AuthResponseDto response = authService.registerStudent(
                new StudentRegisterRequestDto("Andrés Pérez", "andres@polink.edu.co", "Estudiante2026!")
        );

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("andres@polink.edu.co");
        assertThat(response.user().roles()).containsExactly("ROLE_STUDENT");
    }

    @Test
    void registerStudentShouldRejectDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("dup@polink.edu.co")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerStudent(
                new StudentRegisterRequestDto("Dup", "dup@polink.edu.co", "Estudiante2026!")
        )).isInstanceOf(ConflictException.class);
    }

    @Test
    void loginShouldValidatePasswordAndStatus() {
        UUID userId = UUID.randomUUID();
        UserEntity activeUser = UserEntity.builder()
                .id(userId)
                .fullName("Andrés Pérez")
                .email("andres@polink.edu.co")
                .passwordHash("hashed")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .roles(Set.of(new RoleEntity(UUID.randomUUID(), "ROLE_STUDENT")))
                .build();

        when(userRepository.findByEmailIgnoreCase("andres@polink.edu.co")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("Estudiante2026!", "hashed")).thenReturn(true);
        when(jwtService.generateToken(userId, "andres@polink.edu.co", Set.of("ROLE_STUDENT"))).thenReturn("jwt-ok");

        AuthResponseDto ok = authService.login(new LoginRequestDto("andres@polink.edu.co", "Estudiante2026!"));
        assertThat(ok.accessToken()).isEqualTo("jwt-ok");

        when(passwordEncoder.matches("Wrong", "hashed")).thenReturn(false);
        assertThatThrownBy(() -> authService.login(new LoginRequestDto("andres@polink.edu.co", "Wrong")))
                .isInstanceOf(DomainException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        activeUser.setStatus("SUSPENDED");
        when(passwordEncoder.matches("Estudiante2026!", "hashed")).thenReturn(true);
        assertThatThrownBy(() -> authService.login(new LoginRequestDto("andres@polink.edu.co", "Estudiante2026!")))
                .isInstanceOf(DomainException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void forgotAndResetPasswordShouldIssueAndConsumeSingleUseToken() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .fullName("Andrés")
                .email("andres@polink.edu.co")
                .passwordHash("old-hash")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userRepository.findByEmailIgnoreCase("andres@polink.edu.co")).thenReturn(Optional.of(user));

        MessageResponseDto forgotResp = authService.forgotPassword(new ForgotPasswordRequestDto("andres@polink.edu.co"));
        assertThat(forgotResp.message()).contains("instrucciones");

        ArgumentCaptor<PasswordResetTokenEntity> tokenCaptor = ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        PasswordResetTokenEntity issuedToken = tokenCaptor.getValue();
        assertThat(issuedToken.isUsed()).isFalse();

        when(passwordResetTokenRepository.findByTokenAndUsedFalse(issuedToken.getToken()))
                .thenReturn(Optional.of(issuedToken));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NuevaClave2026!")).thenReturn("new-hash");

        MessageResponseDto resetResp = authService.resetPassword(
                new ResetPasswordRequestDto(issuedToken.getToken(), "NuevaClave2026!")
        );

        assertThat(resetResp.message()).contains("exitosamente");
        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(issuedToken.isUsed()).isTrue();
    }

    @Test
    void resetPasswordShouldRejectExpiredToken() {
        PasswordResetTokenEntity expired = PasswordResetTokenEntity.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .token("expired-token")
                .expiresAt(Instant.now().minusSeconds(60))
                .used(false)
                .createdAt(Instant.now().minusSeconds(3600))
                .build();

        when(passwordResetTokenRepository.findByTokenAndUsedFalse("expired-token"))
                .thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequestDto("expired-token", "NuevaClave2026!")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("expirado");
        assertThat(expired.isUsed()).isTrue();
    }
}
