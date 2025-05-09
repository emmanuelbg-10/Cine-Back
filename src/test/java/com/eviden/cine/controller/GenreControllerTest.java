package com.eviden.cine.controller;

import com.eviden.cine.dtos.GenreTranslatedDTO;
import com.eviden.cine.service.GenreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GenreControllerTest {

    private GenreController genreController;

    @Mock
    private GenreService genreService;

    @BeforeEach
    void setUp() {
        genreService = mock(GenreService.class);
        genreController = new GenreController(genreService);
    }

    @Test
    void testGetAllGenres_WithAuthenticatedUser() {
        String email = "user@example.com";
        String acceptLanguage = "fr";

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);

        List<GenreTranslatedDTO> expectedGenres = List.of(new GenreTranslatedDTO(1, "Action"));
        when(genreService.getGenresByUserOrHeader(email, acceptLanguage)).thenReturn(expectedGenres);

        ResponseEntity<List<GenreTranslatedDTO>> response = genreController.getAllGenres(acceptLanguage, auth);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedGenres, response.getBody());
        verify(genreService).getGenresByUserOrHeader(email, acceptLanguage);
    }

    @Test
    void testGetAllGenres_WithNoAuthentication() {
        String acceptLanguage = "en";

        List<GenreTranslatedDTO> expectedGenres = List.of(new GenreTranslatedDTO(1, "Comedia"));
        when(genreService.getGenresByUserOrHeader(null, acceptLanguage)).thenReturn(expectedGenres);

        ResponseEntity<List<GenreTranslatedDTO>> response = genreController.getAllGenres(acceptLanguage, null);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedGenres, response.getBody());
        verify(genreService).getGenresByUserOrHeader(null, acceptLanguage);
    }

    @Test
    void testGetAllGenres_WithNoAuthenticationAndNoHeader() {
        List<GenreTranslatedDTO> expectedGenres = List.of(new GenreTranslatedDTO(1, "Drama"));
        when(genreService.getGenresByUserOrHeader(null, null)).thenReturn(expectedGenres);

        ResponseEntity<List<GenreTranslatedDTO>> response = genreController.getAllGenres(null, null);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedGenres, response.getBody());
        verify(genreService).getGenresByUserOrHeader(null, null);
    }
}
