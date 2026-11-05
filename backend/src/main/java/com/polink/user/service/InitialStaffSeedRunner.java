package com.polink.user.service;

import com.polink.user.entity.RoleEntity;
import com.polink.user.entity.UserEntity;
import com.polink.user.repository.RoleRepository;
import com.polink.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialStaffSeedRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-email:}")
    private String adminEmail;

    @Value("${app.seed.admin-password:}")
    private String adminPassword;

    @Value("${app.seed.supervisor-email:}")
    private String supervisorEmail;

    @Value("${app.seed.supervisor-password:}")
    private String supervisorPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAccountIfMissing(adminEmail, adminPassword, "Administrador Polink", "ROLE_ADMIN");
        seedAccountIfMissing(supervisorEmail, supervisorPassword, "Supervisor Polink", "ROLE_SUPERVISOR");
    }

    private void seedAccountIfMissing(String email, String rawPassword, String fullName, String roleName) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            return;
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return;
        }
        Optional<RoleEntity> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            return;
        }
        Instant now = Instant.now();
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .fullName(fullName)
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .roles(new HashSet<>(Set.of(roleOpt.get())))
                .build();
        userRepository.save(user);
        log.info("Cuenta inicial {} creada para {}", roleName, normalizedEmail);
    }
}
