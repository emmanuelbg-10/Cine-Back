// File: src/test/java/com/eviden/cine/service/AuthServiceTest.java
package com.eviden.cine.service;

import com.eviden.cine.dtos.AuthRequestDTO;
import com.eviden.cine.dtos.AuthResponseDTO;
import com.eviden.cine.dtos.RegisterRequestDTO;
import com.eviden.cine.model.Role;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.RoleRepository;
import com.eviden.cine.repository.UserRepository;
import com.eviden.cine.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDTO registerRequest;
    private AuthRequestDTO authRequest;
    private Role userRole;
    private User user;

    @BeforeEach
    public void setUp() {
        registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password");
        registerRequest.setPreferredLanguage("ES");
        registerRequest.setRegion("RegionTest");

        authRequest = new AuthRequestDTO();
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("password");

        userRole = new Role();
        userRole.setName("USER");

        user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password("encodedPassword")
                .preferredLanguage(registerRequest.getPreferredLanguage())
                .region(registerRequest.getRegion())
                .role(userRole)
                .build();
    }

    @Test
    public void testRegisterSuccess() {
        // Registro exitoso
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User createdUser = authService.register(registerRequest);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo(registerRequest.getEmail());
        assertThat(createdUser.getRole().getName()).isEqualTo("USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testRegisterUserAlreadyExists() {
        // Se retorna usuario existente
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(user));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                authService.register(registerRequest));
        assertThat(exception.getMessage()).contains("Ya existe un usuario con ese email");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testLoginSuccess() {
        // Login exitoso
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(authRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("token123");

        AuthResponseDTO response = authService.login(authRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("token123");
    }

    @Test
    public void testLoginEmailNotFound() {
        // No se encuentra usuario
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.empty());

        Exception exception = assertThrows(BadCredentialsException.class, () ->
                authService.login(authRequest));
        assertThat(exception.getMessage()).contains("Credenciales inválidas");
    }

    @Test
    public void testLoginInvalidPassword() {
        // La contraseña no coincide
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(authRequest.getPassword(), user.getPassword())).thenReturn(false);

        Exception exception = assertThrows(BadCredentialsException.class, () ->
                authService.login(authRequest));
        assertThat(exception.getMessage()).contains("Credenciales inválidas");
    }
}