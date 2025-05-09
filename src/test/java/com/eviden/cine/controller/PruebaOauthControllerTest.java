package com.eviden.cine.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PruebaOauthControllerTest {

    private PruebaOauthcontroller controller;

    @BeforeEach
    void setUp() {
        controller = new PruebaOauthcontroller();
    }

    @Test
    void testPrueba_ReturnsAuthenticatedUsername() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testuser");

        ResponseEntity<String> response = controller.prueba(auth);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Autenticado como: testuser", response.getBody());
    }
}
