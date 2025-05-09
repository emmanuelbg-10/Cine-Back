// File: src/test/java/com/eviden/cine/exception/GlobalExceptionHandlerTest.java
package com.eviden.cine.exception;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleEntityNotFound() {
        EntityNotFoundException ex = new EntityNotFoundException("Entidad no encontrada");
        ResponseEntity<Object> response = handler.handleEntityNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), body.get("status"));
        assertEquals("ENTITY_NOT_FOUND", body.get("code"));
        assertEquals("Entidad no encontrada", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void testHandleCustomException() {
        CustomException ex = new CustomException("Error de validación", "VALIDATION_ERROR");
        ResponseEntity<Object> response = handler.handleCustomException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        assertEquals("VALIDATION_ERROR", body.get("code"));
        assertEquals("Error de validación", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void testHandleSecurity() {
        SecurityException ex = new SecurityException("No autorizado");
        ResponseEntity<Object> response = handler.handleSecurity(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(HttpStatus.FORBIDDEN.value(), body.get("status"));
        assertEquals("FORBIDDEN", body.get("code"));
        assertEquals("No autorizado", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void testHandleAllUnhandledExceptions() {
        Exception ex = new Exception("Error general");
        ResponseEntity<Object> response = handler.handleAllUnhandledExceptions(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.get("status"));
        assertEquals("INTERNAL_ERROR", body.get("code"));
        assertTrue(((String) body.get("message")).contains("Error general"));
        assertNotNull(body.get("timestamp"));
    }
}