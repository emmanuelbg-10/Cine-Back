package com.eviden.cine.service;

import com.eviden.cine.dtos.UpdateProfileDTO;
import com.eviden.cine.model.Role;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.RoleRepository;
import com.eviden.cine.repository.UserRepository;
import com.eviden.cine.security.RandomPWordGenerator;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private RandomPWordGenerator randomPWordGenerator;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testChangeUserRoleByIdSuccessfully() {
        User user = new User();
        Role expectedRole = new Role();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(expectedRole));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.changeUserRoleById(1L, 2L);

        assertNotNull(result);
        assertEquals(expectedRole, result.getRole());
        verify(userRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).findById(2L);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testChangeUserRoleById_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.changeUserRoleById(1L, 2L);
        });
        assertTrue(exception.getMessage().contains("User not found with ID"));
    }

    @Test
    void testChangeUserRoleById_RoleNotFound() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(2L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.changeUserRoleById(1L, 2L);
        });
        assertTrue(exception.getMessage().contains("Role not found with ID"));
    }

    @Test
    void testUpdateProfileSuccessfully() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("oldPassword");

        UpdateProfileDTO updateProfileDTO = new UpdateProfileDTO();
        updateProfileDTO.setEmail("updated@example.com");
        updateProfileDTO.setUsername("UpdatedUsername");
        updateProfileDTO.setPassword("newPassword");
        updateProfileDTO.setPreferredLanguage("en");
        updateProfileDTO.setRegion("US");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User updatedUser = userService.updateProfile(updateProfileDTO, "test@example.com");

        assertEquals("UpdatedUsername", updatedUser.getUsername());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("encodedPassword", updatedUser.getPassword());
        assertEquals("en", updatedUser.getPreferredLanguage());
        assertEquals("US", updatedUser.getRegion());
    }

    @Test
    void testSendResetToken() throws MessagingException {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // No se espera llamada a save, pues el servicio solo envía el email
        userService.sendResetToken(email);
        verify(emailService, times(1)).sendPasswordResetEmail(eq(email), anyString());
    }

    @Test
    void testResetPasswordSuccessfully() {
        String token = "valid-token";
        String email = "test@example.com";

        // Forzar que el token esté en resetTokens
        userService.getResetTokens().put(token, email);

        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.resetPassword(token, "newPassword");

        verify(userRepository, times(1)).save(user);
        assertEquals("encodedPassword", user.getPassword());
        assertFalse(userService.getResetTokens().containsKey(token));
    }

    @Test
    void testResetPasswordInvalidToken() {
        String invalidToken = "invalid-token";

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.resetPassword(invalidToken, "newPassword")
        );

        assertTrue(exception.getMessage().contains("Token inválido o expirado"));
        verify(userRepository, never()).save(any(User.class));
    }



    @Test
    void testFindOrCreateUser_UserExists() {
        User user = new User();
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(user));
        // Si findOrCreateUser ahora espera un OAuth2User, se pasa null para caso existente.
        User result = userService.findOrCreateUser("existing@example.com", (OAuth2User) null);
        assertEquals(user, result);
    }

    @Test
    void testFindOrCreateUser_UserDoesNotExist() {
        String email = "new@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("name")).thenReturn("Test Name");

        Role role = new Role();
        role.setName("USER");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));

        User savedUser = new User();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        try (MockedStatic<RandomPWordGenerator> mocked = mockStatic(RandomPWordGenerator.class)) {
            mocked.when(RandomPWordGenerator::generate).thenReturn("generatedPassword");

            User result = userService.findOrCreateUser(email, oAuth2User);

            assertNotNull(result);
            verify(userRepository, times(1)).save(any(User.class));
        }
    }

}