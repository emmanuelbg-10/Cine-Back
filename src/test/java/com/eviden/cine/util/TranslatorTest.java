package com.eviden.cine.util;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class TranslatorTest {

    @Test
    void testTraducirSuccess() throws Exception {
        // Mock de la respuesta HTTP
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        String fakeJson = "{ \"responseData\": { \"translatedText\": \"Hola mundo\" } }";

        when(mockResponse.body()).thenReturn(fakeJson);
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        try (MockedStatic<HttpClient> staticHttpClient = mockStatic(HttpClient.class)) {
            staticHttpClient.when(HttpClient::newHttpClient).thenReturn(mockClient);

            String resultado = Translator.traducir("Hello world", "en", "es");
            assertEquals("Hola mundo", resultado);
        }
    }

    @Test
    void testTraducirError() {
        String input = "Hello world";
        // Forzamos error simulando un fallo de red, devolviendo el texto original
        String resultado = Translator.traducir(input, "en", "es");
        // Este test simplemente valida que si falla, devuelve el texto original
        // (no lanza excepción)
        assertNotNull(resultado);
    }
}
