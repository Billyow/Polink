package com.polink.common.security;

import com.polink.user.entity.RoleEntity;
import com.polink.user.entity.UserEntity;
import com.polink.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsernameShouldMapActiveAndSuspendedUsersAndThrowWhenNotFound() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .fullName("Andrés Pérez")
                .email("andres@polink.edu.co")
                .passwordHash("hashed")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .roles(Set.of(new RoleEntity(UUID.randomUUID(), "ROLE_STUDENT")))
                .build();

        when(userRepository.findByEmailIgnoreCase("andres@polink.edu.co")).thenReturn(Optional.of(user));

        UserDetails activeDetails = customUserDetailsService.loadUserByUsername("  andres@polink.edu.co  ");
        assertThat(activeDetails.getUsername()).isEqualTo("andres@polink.edu.co");
        assertThat(activeDetails.isEnabled()).isTrue();
        assertThat(activeDetails.getAuthorities()).extracting("authority").containsExactly("ROLE_STUDENT");

        user.setStatus("SUSPENDED");
        UserDetails suspendedDetails = customUserDetailsService.loadUserByUsername("andres@polink.edu.co");
        assertThat(suspendedDetails.isEnabled()).isFalse();

        when(userRepository.findByEmailIgnoreCase("missing@polink.edu.co")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("missing@polink.edu.co"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
