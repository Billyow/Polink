package com.polink.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polink.auth.dto.AuthResponseDto;
import com.polink.auth.dto.ForgotPasswordRequestDto;
import com.polink.auth.dto.LoginRequestDto;
import com.polink.auth.dto.MessageResponseDto;
import com.polink.auth.dto.ResetPasswordRequestDto;
import com.polink.auth.dto.StudentRegisterRequestDto;
import com.polink.auth.service.AuthService;
import com.polink.common.config.SecurityConfig;
import com.polink.common.exception.GlobalExceptionHandler;
import com.polink.user.dto.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void registerLoginForgotAndResetEndpointsShouldBePublicAndReturnExpectedStatus() throws Exception {
        AuthResponseDto authResponse = new AuthResponseDto(
                "jwt-123",
                "Bearer",
                86400000L,
                new UserResponseDto(
                        UUID.randomUUID(),
                        "Andrés Pérez",
                        "andres@polink.edu.co",
                        "ACTIVE",
                        Set.of("ROLE_STUDENT"),
                        Instant.now()
                )
        );

        when(authService.registerStudent(any(StudentRegisterRequestDto.class))).thenReturn(authResponse);
        when(authService.login(any(LoginRequestDto.class))).thenReturn(authResponse);
        when(authService.forgotPassword(any(ForgotPasswordRequestDto.class)))
                .thenReturn(new MessageResponseDto("Instrucciones enviadas"));
        when(authService.resetPassword(any(ResetPasswordRequestDto.class)))
                .thenReturn(new MessageResponseDto("Contraseña restablecida"));

        mockMvc.perform(post("/api/v1/auth/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new StudentRegisterRequestDto("Andrés Pérez", "andres@polink.edu.co", "Estudiante2026!")
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("jwt-123"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequestDto("andres@polink.edu.co", "Estudiante2026!")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ForgotPasswordRequestDto("andres@polink.edu.co")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Instrucciones enviadas"));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ResetPasswordRequestDto("token-valido", "NuevaClave2026!")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Contraseña restablecida"));
    }
}
