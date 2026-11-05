package com.polink.auth.controller;

import com.polink.auth.dto.AuthResponseDto;
import com.polink.auth.dto.ForgotPasswordRequestDto;
import com.polink.auth.dto.LoginRequestDto;
import com.polink.auth.dto.MessageResponseDto;
import com.polink.auth.dto.ResetPasswordRequestDto;
import com.polink.auth.dto.StudentRegisterRequestDto;
import com.polink.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registro de estudiante, inicio de sesión con JWT y recuperación de contraseña")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/student")
    @Operation(summary = "Registrar cuenta de estudiante", description = "Crea una nueva cuenta con rol ROLE_STUDENT y retorna un JWT activo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Estudiante registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "409", description = "El correo electrónico ya está registrado")
    })
    public ResponseEntity<AuthResponseDto> registerStudent(@Valid @RequestBody StudentRegisterRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerStudent(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Valida credenciales y estado activo de la cuenta, retornando el token JWT y el perfil del usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
            @ApiResponse(responseCode = "403", description = "Cuenta suspendida")
    })
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperación de contraseña", description = "Genera un token temporal de 30 minutos y lo envía por correo e In-App si la cuenta existe y está activa.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud procesada")
    })
    public ResponseEntity<MessageResponseDto> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña con token", description = "Valida el token temporal de un solo uso y actualiza la contraseña del usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contraseña restablecida exitosamente"),
            @ApiResponse(responseCode = "400", description = "Token inválido, ya utilizado o expirado")
    })
    public ResponseEntity<MessageResponseDto> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
