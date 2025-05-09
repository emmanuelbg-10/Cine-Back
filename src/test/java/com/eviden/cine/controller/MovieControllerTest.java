package com.eviden.cine.controller;

import com.eviden.cine.dtos.MovieDTO;
import com.eviden.cine.dtos.MovieTranslatedDTO;
import com.eviden.cine.model.Movie;
import com.eviden.cine.service.CloudinaryService;
import com.eviden.cine.service.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MovieControllerTest {

    @Mock
    private MovieService movieService;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private MovieController movieController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllMoviesTranslatedTest() {
        when(movieService.getAllMovies()).thenReturn(Collections.emptyList());
        when(movieService.translateMovies(anyList(), anyString())).thenReturn(Collections.emptyList());
        ResponseEntity<List<MovieTranslatedDTO>> response = movieController.getTranslatedMovies(null, "en");
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void getMovieByIdTranslatedTest_notFound() {
        when(movieService.getMovieById(anyInt())).thenReturn(Optional.empty());
        ResponseEntity<MovieTranslatedDTO> response = movieController.getMovieByIdTranslated(null, "en", 1);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void getMovieByTitleTest_found() {
        Movie movie = new Movie();
        when(movieService.getMovieByTitle(anyString())).thenReturn(Optional.of(movie));
        ResponseEntity<Movie> response = movieController.getMovieByTitle("Test");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void getMovieByTitleTest_notFound() {
        when(movieService.getMovieByTitle(anyString())).thenReturn(Optional.empty());
        ResponseEntity<Movie> response = movieController.getMovieByTitle("Test");
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void searchMoviesTest() {
        // Mocks necesarios para los servicios
        when(movieService.searchByKeyword(any())).thenReturn(Collections.emptyList());
        when(movieService.translateMovies(any(), any())).thenReturn(Collections.emptyList());

        // Llamar al controlador pasando los parámetros necesarios
        ResponseEntity<List<MovieTranslatedDTO>> response = movieController.searchMovies("keyword", "en", null);

        // Verificaciones
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());  // Verificar que la lista esté vacía
    }


    @Test
    void getMoviesByGenreTest() {
        when(movieService.getMoviesByGenre(anyString())).thenReturn(Collections.emptyList());
        ResponseEntity<List<Movie>> response = movieController.getMoviesByGenre("Action");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void getAvailableMoviesTest() {
        when(movieService.getAvailableMovies()).thenReturn(Collections.emptyList());
        ResponseEntity<List<Movie>> response = movieController.getAvailableMovies();
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void getComingSoonMoviesTest() {
        when(movieService.getComingSoonMovies()).thenReturn(Collections.emptyList());
        when(movieService.translateMovies(anyList(), anyString())).thenReturn(Collections.emptyList());
        ResponseEntity<List<MovieTranslatedDTO>> response = movieController.getComingSoonMovies(null, "en");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void createMovieTest() throws IOException {
        MockMultipartFile imageX = new MockMultipartFile("imageX", "filenameX.jpg", "image/jpeg", "imageX content".getBytes());
        MockMultipartFile imageY = new MockMultipartFile("imageY", "filenameY.jpg", "image/jpeg", "imageY content".getBytes());
        Movie movie = new Movie();

        when(cloudinaryService.uploadImage(any(), anyInt(), anyInt())).thenReturn("http://image.com/x", "http://image.com/y");
        when(movieService.createMovie(any(MovieDTO.class), anyString())).thenReturn(movie);

        MovieDTO dto = new MovieDTO();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String movieDTOJson = mapper.writeValueAsString(dto);

        ResponseEntity<Movie> response = movieController.createMovie(imageX, imageY, movieDTOJson, "en");
        assertEquals(201, response.getStatusCodeValue());
        assertEquals(movie, response.getBody());
    }

    @Test
    void updateMovieTest_found() throws IOException {
        Movie existing = new Movie();
        Movie updated = new Movie();
        when(movieService.getMovieById(anyInt())).thenReturn(Optional.of(existing));
        when(movieService.updateMovie(anyInt(), any(MovieDTO.class), anyString())).thenReturn(updated);

        MovieDTO dto = new MovieDTO();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String movieDTOJson = mapper.writeValueAsString(dto);

        ResponseEntity<?> response = movieController.updateMovie(1, null, null, movieDTOJson, "en");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void updateMovieTest_notFound() throws IOException {
        when(movieService.getMovieById(anyInt())).thenReturn(Optional.empty());

        MovieDTO dto = new MovieDTO();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String movieDTOJson = mapper.writeValueAsString(dto);

        ResponseEntity<?> response = movieController.updateMovie(1, null, null, movieDTOJson, "en");
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void deleteMovieTest() {
        doNothing().when(movieService).deleteMovieById(anyInt());
        ResponseEntity<Void> response = movieController.deleteMovie(1);
        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void filterMoviesWithTranslationTest() {
        when(movieService.filterMovies(any(Specification.class))).thenReturn(Collections.emptyList());
        when(movieService.translateMovies(anyList(), anyString())).thenReturn(Collections.emptyList());
        ResponseEntity<List<MovieTranslatedDTO>> response = movieController.filterMoviesWithTranslation(null, "en", "Action", null, null, null, false, null);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void getTranslatedMoviesTest() {
        when(movieService.getAllMovies()).thenReturn(Collections.emptyList());
        when(movieService.translateMovies(anyList(), anyString())).thenReturn(Collections.emptyList());
        ResponseEntity<List<MovieTranslatedDTO>> response = movieController.getTranslatedMovies(null, "en");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void getBillboardMoviesTest() {
        when(movieService.getMoviesForBillboard()).thenReturn(Collections.emptyList());
        ResponseEntity<List<Movie>> response = movieController.getBillboardMovies();
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void getAllMoviesTranslated_withAuth() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");
        when(movieService.getAllMovies()).thenReturn(Collections.emptyList());
        when(movieService.translateMovies(anyList(), anyString())).thenReturn(Collections.emptyList());

        ResponseEntity<List<MovieTranslatedDTO>> response = movieController.getAllMoviesTranslated(auth, "en");
        assertEquals(200, response.getStatusCodeValue());
    }







    @Test
    void searchMovies_emptyQuery_returnsBadRequest() {
        ResponseEntity<List<MovieTranslatedDTO>> response = movieController.searchMovies(" ", "en", null);
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void updateMovie_withImageX() throws Exception {
        MockMultipartFile imageX = new MockMultipartFile("imageX", "fileX.jpg", "image/jpeg", "data".getBytes());
        Movie existing = new Movie();
        existing.setUrlImageX("http://cloudinary.com/old_image.jpg");

        when(movieService.getMovieById(anyInt())).thenReturn(Optional.of(existing));
        when(movieService.updateMovie(anyInt(), any(MovieDTO.class), anyString())).thenReturn(existing);

        MovieDTO dto = new MovieDTO();
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String movieDTOJson = mapper.writeValueAsString(dto);

        ResponseEntity<?> response = movieController.updateMovie(1, imageX, null, movieDTOJson, "en");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void updateMovie_jsonProcessingException() {
        String invalidJson = "{invalid_json}";

        ResponseEntity<?> response = movieController.updateMovie(1, null, null, invalidJson, "en");
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void updateMovie_generalException() throws Exception {
        Movie existing = new Movie();
        when(movieService.getMovieById(anyInt())).thenReturn(Optional.of(existing));
        when(movieService.updateMovie(anyInt(), any(MovieDTO.class), anyString())).thenThrow(new RuntimeException("Error"));

        MovieDTO dto = new MovieDTO();
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String movieDTOJson = mapper.writeValueAsString(dto);

        ResponseEntity<?> response = movieController.updateMovie(1, null, null, movieDTOJson, "en");
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    void updateMovie_withImageY() throws Exception {
        MockMultipartFile imageY = new MockMultipartFile("imageY", "fileY.jpg", "image/jpeg", "data".getBytes());
        Movie existing = new Movie();
        existing.setUrlImageY("http://cloudinary.com/old_imageY.jpg");

        when(movieService.getMovieById(anyInt())).thenReturn(Optional.of(existing));
        when(movieService.updateMovie(anyInt(), any(MovieDTO.class), anyString())).thenReturn(existing);

        MovieDTO dto = new MovieDTO();
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String movieDTOJson = mapper.writeValueAsString(dto);

        ResponseEntity<?> response = movieController.updateMovie(1, null, imageY, movieDTOJson, "en");
        assertEquals(200, response.getStatusCodeValue());
    }







}