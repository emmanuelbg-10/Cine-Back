package com.eviden.cine.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CustomExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Algo salió mal";
        CustomException ex = new CustomException(message);

        assertEquals(message, ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Error con causa";
        Throwable cause = new Exception("Causa original");

        CustomException ex = new CustomException(message,null, cause);

        assertEquals(message, ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}
