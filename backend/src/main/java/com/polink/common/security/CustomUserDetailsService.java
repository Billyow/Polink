package com.polink.common.security;

import com.polink.user.entity.RoleEntity;
import com.polink.user.entity.UserEntity;
import com.polink.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        Set<String> roleNames = user.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toUnmodifiableSet());

        return new SecurityUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                "ACTIVE".equalsIgnoreCase(user.getStatus()),
                roleNames
        );
    }
}
