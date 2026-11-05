package com.polink.common.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternalShouldAuthenticateWhenTokenIsValidAndUserIsActiveInDatabase() throws Exception {
        UUID userId = UUID.randomUUID();
        SecurityUserPrincipal tokenPrincipal = new SecurityUserPrincipal(
                userId, "active@polink.edu.co", "", true, Set.of("ROLE_STUDENT")
        );
        SecurityUserPrincipal dbPrincipal = new SecurityUserPrincipal(
                userId, "active@polink.edu.co", "hash", true, Set.of("ROLE_STUDENT", "ROLE_TUTOR")
        );

        when(jwtService.isTokenValid("valid-jwt")).thenReturn(true);
        when(jwtService.extractPrincipal("valid-jwt")).thenReturn(tokenPrincipal);
        when(userDetailsService.loadUserByUsername("active@polink.edu.co")).thenReturn(dbPrincipal);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(dbPrincipal);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternalShouldRejectWhenUserIsSuspendedOrDeletedInDatabase() throws Exception {
        UUID userId = UUID.randomUUID();
        SecurityUserPrincipal tokenPrincipal = new SecurityUserPrincipal(
                userId, "suspended@polink.edu.co", "", true, Set.of("ROLE_STUDENT")
        );
        SecurityUserPrincipal suspendedDbPrincipal = new SecurityUserPrincipal(
                userId, "suspended@polink.edu.co", "hash", false, Set.of("ROLE_STUDENT")
        );

        when(jwtService.isTokenValid("valid-jwt")).thenReturn(true);
        when(jwtService.extractPrincipal("valid-jwt")).thenReturn(tokenPrincipal);
        when(userDetailsService.loadUserByUsername("suspended@polink.edu.co")).thenReturn(suspendedDbPrincipal);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        when(userDetailsService.loadUserByUsername("suspended@polink.edu.co"))
                .thenThrow(new UsernameNotFoundException("No existe"));
        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternalShouldSkipWhenNoBearerHeaderAndFallbackToTokenPrincipalWhenNoUserDetailsService() throws Exception {
        JwtAuthenticationFilter filterWithoutUserDetails = new JwtAuthenticationFilter(jwtService);
        MockHttpServletRequest noHeaderReq = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filterWithoutUserDetails.doFilterInternal(noHeaderReq, response, filterChain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        SecurityUserPrincipal tokenPrincipal = new SecurityUserPrincipal(
                UUID.randomUUID(), "fallback@polink.edu.co", "", true, Set.of("ROLE_STUDENT")
        );
        when(jwtService.isTokenValid("token-only")).thenReturn(true);
        when(jwtService.extractPrincipal("token-only")).thenReturn(tokenPrincipal);

        MockHttpServletRequest tokenReq = new MockHttpServletRequest();
        tokenReq.addHeader("Authorization", "Bearer token-only");
        filterWithoutUserDetails.doFilterInternal(tokenReq, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }
}
