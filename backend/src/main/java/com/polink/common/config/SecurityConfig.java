package com.polink.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polink.common.exception.ApiErrorResponse;
import com.polink.common.security.CustomUserDetailsService;
import com.polink.common.security.JwtAuthenticationFilter;
import com.polink.common.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectProvider<JwtService> jwtServiceProvider,
            ObjectProvider<CustomUserDetailsService> userDetailsServiceProvider,
            ObjectProvider<ObjectMapper> objectMapperProvider
    ) throws Exception {
        ObjectMapper mapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            ApiErrorResponse errorBody = new ApiErrorResponse(
                                    Instant.now(),
                                    HttpStatus.UNAUTHORIZED.value(),
                                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                                    "Autenticación requerida o sesión inválida",
                                    request.getRequestURI(),
                                    Map.of()
                            );
                            mapper.writeValue(response.getWriter(), errorBody);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            ApiErrorResponse errorBody = new ApiErrorResponse(
                                    Instant.now(),
                                    HttpStatus.FORBIDDEN.value(),
                                    HttpStatus.FORBIDDEN.getReasonPhrase(),
                                    "No tienes permisos para realizar esta acción",
                                    request.getRequestURI(),
                                    Map.of()
                            );
                            mapper.writeValue(response.getWriter(), errorBody);
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/uploads/**",
                                "/api/v1/auth/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/catalog/**", "/api/v1/offerings/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/v1/supervisor/**").hasAuthority("ROLE_SUPERVISOR")
                        .requestMatchers("/api/v1/tutor/**").hasAuthority("ROLE_TUTOR")
                        .anyRequest().authenticated()
                );

        JwtService jwtService = jwtServiceProvider.getIfAvailable();
        if (jwtService != null) {
            CustomUserDetailsService userDetailsService = userDetailsServiceProvider.getIfAvailable();
            http.addFilterBefore(
                    new JwtAuthenticationFilter(jwtService, userDetailsService),
                    UsernamePasswordAuthenticationFilter.class
            );
        }

        return http.build();
    }
}
