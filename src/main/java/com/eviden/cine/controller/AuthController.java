package com.eviden.cine.controller;

import com.eviden.cine.dtos.*;
import com.eviden.cine.model.User;
import com.eviden.cine.service.AuthService;
import com.eviden.cine.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Operaciones relacionadas con el acceso y recuperación de contraseña")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Registro de nuevo usuario", description = "Crea un nuevo usuario con rol USER por defecto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario registrado correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    public ResponseEntity<User> register(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del nuevo usuario",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegisterRequestDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "username": "pablo",
                              "email": "pablo@mail.com",
                              "password": "claveSegura123",
                              "preferredLanguage": "es",
                              "region": "ES"
                            }
                            """))
            ) RegisterRequestDTO request) {
        User newUser = authService.register(request);
        return ResponseEntity.ok(newUser);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve un token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content)
    })
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciales del usuario",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AuthRequestDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "email": "pablo@mail.com",
                              "password": "claveSegura123"
                            }
                            """))
            ) AuthRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperación de contraseña", description = "Envía un correo con un enlace para restablecer la contraseña.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Correo enviado correctamente"),
            @ApiResponse(responseCode = "404", description = "Correo no encontrado")
    })
    public ResponseEntity<String> forgotPassword(
            @RequestBody @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email del usuario que quiere recuperar su contraseña",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ForgotPasswordRequestDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "email": "pablo@mail.com"
                            }
                            """))
            ) ForgotPasswordRequestDTO request) {
        userService.sendResetToken(request.getEmail());
        return ResponseEntity.ok("Se ha enviado un enlace de recuperación a tu correo");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña", description = "Permite actualizar la contraseña con un token enviado previamente por email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Token inválido o expirado")
    })
    public ResponseEntity<String> resetPassword(
            @RequestBody @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Token de recuperación y nueva contraseña",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ResetPasswordRequestDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "token": "123e4567-e89b-12d3-a456-426614174000",
                              "newPassword": "nuevaClave123"
                            }
                            """))
            ) ResetPasswordRequestDTO request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }
}
