package com.polink.common.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class SecurityUserPrincipal implements UserDetails {

    private final UUID userId;
    private final String email;
    private final String passwordHash;
    private final boolean active;
    private final Set<String> roleNames;
    private final Collection<? extends GrantedAuthority> authorities;

    public SecurityUserPrincipal(UUID userId, String email, String passwordHash, boolean active, Set<String> roleNames) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = active;
        this.roleNames = roleNames;
        this.authorities = roleNames.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
