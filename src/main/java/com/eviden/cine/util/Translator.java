package com.eviden.cine.util;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class Translator {

    private static final Logger logger = LoggerFactory.getLogger(Translator.class);
    private static final String API_URL = "https://api.mymemory.translated.net/get";
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static String traducir(String texto, String from, String to) {
        try {
            String query = "?q=" + URLEncoder.encode(texto, StandardCharsets.UTF_8)
                    + "&langpair=" + URLEncoder.encode(from + "|" + to, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + query))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            JSONObject json = new JSONObject(response.body());
            return json.getJSONObject("responseData").getString("translatedText");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Reinterrumpir el hilo
            logger.error("La solicitud de traducción fue interrumpida para el texto: {}", texto, e);
            return texto;
        } catch (Exception e) {
            logger.error("Error al traducir texto: {}", texto, e);
            return texto;
        }
    }
}
