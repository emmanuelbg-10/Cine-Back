package com.eviden.cine.controller;

import com.eviden.cine.model.User;
import com.eviden.cine.repository.UserRepository;
import com.eviden.cine.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestAuthControllerTest {

    private TestAuthController controller;
    private JwtUtil jwtUtil;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        userRepository = mock(UserRepository.class);
        controller = new TestAuthController(jwtUtil, userRepository);
    }

    @Test
    void testGetAllSeededTokens() {
        // Usuarios esperados
        String[] emails = {
                "juanito@mail.com",
                "marta@mail.com",
                "carlos89@mail.com",
                "lucia23@mail.com",
                "adminpro@mail.com"
        };

        // Simulamos que para algunos usuarios los encontramos y para otros no
        for (String email : emails) {
            if (!email.equals("carlos89@mail.com")) {
                User user = new User();
                user.setEmail(email);
                when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
                when(jwtUtil.generateToken(user)).thenReturn(email + "_token");
            } else {
                when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
            }
        }

        ResponseEntity<Map<String, String>> response = controller.getAllSeededTokens();

        assertEquals(200, response.getStatusCodeValue());
        Map<String, String> tokens = response.getBody();
        assertNotNull(tokens);
        assertEquals(5, tokens.size());

        assertTrue(tokens.get("juanito@mail.com").startsWith("Bearer "));
        assertTrue(tokens.get("marta@mail.com").startsWith("Bearer "));
        assertEquals("Usuario no encontrado", tokens.get("carlos89@mail.com"));
        assertTrue(tokens.get("lucia23@mail.com").startsWith("Bearer "));
        assertTrue(tokens.get("adminpro@mail.com").startsWith("Bearer "));
    }
}
