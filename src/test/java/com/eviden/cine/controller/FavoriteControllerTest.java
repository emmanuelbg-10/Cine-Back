package com.eviden.cine.controller;

import com.eviden.cine.dtos.FavoriteDTO;
import com.eviden.cine.dtos.FavoriteTranslateDTO;
import com.eviden.cine.model.Favorite;
import com.eviden.cine.model.Movie;
import com.eviden.cine.model.User;
import com.eviden.cine.service.FavoriteService;
import com.eviden.cine.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FavoriteControllerTest {

    @Mock
    private FavoriteService favoriteService;

    @Mock
    private MovieService movieService;

    @InjectMocks
    private FavoriteController favoriteController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetFavoritesByUserId() {
        FavoriteTranslateDTO dto = new FavoriteTranslateDTO();
        when(movieService.resolveLanguage(any(), any())).thenReturn("en");
        when(favoriteService.getFavoriteTranslated(eq(1L), eq("en")))
                .thenReturn(List.of(dto));

        ResponseEntity<List<FavoriteTranslateDTO>> response = favoriteController.getFavoritesByUserId(null, "en", 1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        verify(favoriteService).getFavoriteTranslated(1L, "en");
    }

    @Test
    void testGetFavoriteById_found() {
        Favorite favorite = new Favorite();
        when(favoriteService.getFavoriteById(1L)).thenReturn(favorite);

        ResponseEntity<Favorite> response = favoriteController.getFavoriteById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(favorite, response.getBody());
    }

    @Test
    void testGetFavoriteById_notFound() {
        when(favoriteService.getFavoriteById(1L)).thenReturn(null);

        ResponseEntity<Favorite> response = favoriteController.getFavoriteById(1L);

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testAddFavorite_success() {
        Favorite favorite = new Favorite();
        User user = new User();
        user.setUserId(1L);
        Movie movie = new Movie();
        movie.setId(2);
        favorite.setUser(user);
        favorite.setMovie(movie);

        when(favoriteService.addFavorite(1L, 2)).thenReturn(favorite);

        ResponseEntity<Favorite> response = favoriteController.addFavorite(favorite);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(favorite, response.getBody());
    }

    @Test
    void testAddFavorite_conflict() {
        Favorite favorite = new Favorite();
        User user = new User();
        user.setUserId(1L);
        Movie movie = new Movie();
        movie.setId(2);
        favorite.setUser(user);
        favorite.setMovie(movie);

        when(favoriteService.addFavorite(1L, 2))
                .thenThrow(new com.eviden.cine.exception.CustomException("ya existe"));

        ResponseEntity<Favorite> response = favoriteController.addFavorite(favorite);

        assertEquals(409, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testAddFavorite_badRequest() {
        Favorite favorite = new Favorite();
        User user = new User();
        user.setUserId(1L);
        Movie movie = new Movie();
        movie.setId(2);
        favorite.setUser(user);
        favorite.setMovie(movie);

        when(favoriteService.addFavorite(1L, 2)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<Favorite> response = favoriteController.addFavorite(favorite);

        assertEquals(400, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testRemoveFavorite_success() {
        Favorite favorite = new Favorite();
        User user = new User();
        user.setUserId(1L);
        Movie movie = new Movie();
        movie.setId(2);
        favorite.setUser(user);
        favorite.setMovie(movie);

        doNothing().when(favoriteService).removeFavorite(1L, 2);

        ResponseEntity<Void> response = favoriteController.removeFavorite(favorite);

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testRemoveFavorite_error() {
        Favorite favorite = new Favorite();
        User user = new User();
        user.setUserId(1L);
        Movie movie = new Movie();
        movie.setId(2);
        favorite.setUser(user);
        favorite.setMovie(movie);

        doThrow(new com.eviden.cine.exception.CustomException("Error")).when(favoriteService).removeFavorite(1L, 2);

        ResponseEntity<Void> response = favoriteController.removeFavorite(favorite);

        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testGetFavoritesByEmail() {
        FavoriteDTO dto = new FavoriteDTO();
        when(favoriteService.getFavoritesByEmail("test@example.com"))
                .thenReturn(List.of(dto));

        ResponseEntity<List<FavoriteDTO>> response =
                favoriteController.getFavoritesByEmail("test@example.com");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        verify(favoriteService).getFavoritesByEmail("test@example.com");
    }
}
