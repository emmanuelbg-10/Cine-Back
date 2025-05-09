package com.eviden.cine.controller;

import com.eviden.cine.dtos.UpdateProfileDTO;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.UserRepository;
import com.eviden.cine.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private UserController controller;
    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);
        controller = new UserController(userRepository, userService);
    }

    @Test
    void testGetAllUsers() {
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        ResponseEntity<List<User>> response = controller.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
    }

    @Test
    void testGetUserById_Found() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<User> response = controller.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<User> response = controller.getUserById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testUpdateProfile_Success() {
        UpdateProfileDTO dto = new UpdateProfileDTO();
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");

        User updatedUser = new User();
        updatedUser.setEmail("user@example.com");

        when(userService.updateProfile(dto, "user@example.com")).thenReturn(updatedUser);

        ResponseEntity<?> response = controller.updateProfile(dto, auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedUser, response.getBody());
    }

    @Test
    void testUpdateProfile_Exception() {
        UpdateProfileDTO dto = new UpdateProfileDTO();
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");

        when(userService.updateProfile(dto, "user@example.com")).thenThrow(new RuntimeException("Test Error"));

        ResponseEntity<?> response = controller.updateProfile(dto, auth);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertEquals("RuntimeException", errorResponse.get("error"));
        assertEquals("Test Error", errorResponse.get("message"));
    }

    @Test
    void testChangeUserRoleById_Success() {
        User user = new User();
        when(userService.changeUserRoleById(1L, 2L)).thenReturn(user);

        ResponseEntity<User> response = controller.changeUserRoleById(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    void testChangeUserRoleById_Failure() {
        when(userService.changeUserRoleById(1L, 2L)).thenThrow(new RuntimeException("Role change failed"));

        ResponseEntity<User> response = controller.changeUserRoleById(1L, 2L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }
}
