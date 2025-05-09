// File: src/test/java/com/eviden/cine/controller/AuthControllerTest.java
package com.eviden.cine.controller;

import com.eviden.cine.dtos.*;
import com.eviden.cine.model.User;
import com.eviden.cine.service.AuthService;
import com.eviden.cine.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequestDTO registerRequest;
    private AuthRequestDTO authRequest;
    private ForgotPasswordRequestDTO forgotPasswordRequest;
    private ResetPasswordRequestDTO resetPasswordRequest;
    private User user;
    private AuthResponseDTO authResponse;

    @BeforeEach
    public void setUp() {
        registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("pablo");
        registerRequest.setEmail("pablo@mail.com");
        registerRequest.setPassword("claveSegura123");
        registerRequest.setPreferredLanguage("es");
        registerRequest.setRegion("ES");

        user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());

        authRequest = new AuthRequestDTO();
        authRequest.setEmail("pablo@mail.com");
        authRequest.setPassword("claveSegura123");

        authResponse = new AuthResponseDTO();
        authResponse.setToken("token123");

        forgotPasswordRequest = new ForgotPasswordRequestDTO();
        forgotPasswordRequest.setEmail("pablo@mail.com");

        resetPasswordRequest = new ResetPasswordRequestDTO();
        resetPasswordRequest.setToken("123e4567-e89b-12d3-a456-426614174000");
        resetPasswordRequest.setNewPassword("nuevaClave123");
    }

    @Test
    public void testRegisterSuccess() {
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(user);

        ResponseEntity<User> response = authController.register(registerRequest);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(user);
        verify(authService, times(1)).register(registerRequest);
    }

    @Test
    public void testLoginSuccess() {
        when(authService.login(any(AuthRequestDTO.class))).thenReturn(authResponse);

        ResponseEntity<AuthResponseDTO> response = authController.login(authRequest);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(authResponse);
        verify(authService, times(1)).login(authRequest);
    }

    @Test
    public void testForgotPasswordSuccess() {
        // Se simula la acción de enviar token de reseteo sin retorno de valor.
        doNothing().when(userService).sendResetToken(forgotPasswordRequest.getEmail());

        ResponseEntity<String> response = authController.forgotPassword(forgotPasswordRequest);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Se ha enviado un enlace de recuperación a tu correo");
        verify(userService, times(1)).sendResetToken(forgotPasswordRequest.getEmail());
    }

    @Test
    public void testResetPasswordSuccess() {
        // Se simula la acción de actualizar la contraseña sin retorno de valor.
        doNothing().when(userService).resetPassword(resetPasswordRequest.getToken(), resetPasswordRequest.getNewPassword());

        ResponseEntity<String> response = authController.resetPassword(resetPasswordRequest);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Contraseña actualizada correctamente");
        verify(userService, times(1)).resetPassword(resetPasswordRequest.getToken(), resetPasswordRequest.getNewPassword());
    }
}