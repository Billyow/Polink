package com.polink.user.service;

import com.polink.user.entity.RoleEntity;
import com.polink.user.entity.UserEntity;
import com.polink.user.repository.RoleRepository;
import com.polink.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitialStaffSeedRunnerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private InitialStaffSeedRunner runner;

    @Test
    void runShouldSeedAdminAndSupervisorIdempotently() {
        ReflectionTestUtils.setField(runner, "adminEmail", "admin@polink.edu.co");
        ReflectionTestUtils.setField(runner, "adminPassword", "Admin2026!");
        ReflectionTestUtils.setField(runner, "supervisorEmail", "supervisor@polink.edu.co");
        ReflectionTestUtils.setField(runner, "supervisorPassword", "Supervisor2026!");

        RoleEntity adminRole = new RoleEntity(UUID.randomUUID(), "ROLE_ADMIN");
        when(userRepository.existsByEmailIgnoreCase("admin@polink.edu.co")).thenReturn(false);
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode("Admin2026!")).thenReturn("hashed-admin");

        when(userRepository.existsByEmailIgnoreCase("supervisor@polink.edu.co")).thenReturn(true);

        runner.run(new DefaultApplicationArguments());

        verify(userRepository, times(1)).save(any(UserEntity.class));
    }
}
