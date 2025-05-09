package com.eviden.cine.service;

import com.eviden.cine.dtos.FavoriteDTO;
import com.eviden.cine.exception.CustomException;
import com.eviden.cine.model.Favorite;
import com.eviden.cine.model.Movie;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.FavoriteRepository;
import com.eviden.cine.repository.MovieRepository;
import com.eviden.cine.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetFavoritesDTOByUserId() {
        Movie movie = Movie.builder().title("Matrix").build();
        User user = User.builder().userId(1L).build();

        Favorite favorite = Favorite.builder()
                .id(1L)
                .user(user)
                .movie(movie)
                .addedDate(LocalDateTime.now())
                .build();

        when(favoriteRepository.findByUserUserId(1L)).thenReturn(List.of(favorite));

        List<FavoriteDTO> result = favoriteService.getFavoritesDTOByUserId(1L);

        assertEquals(1, result.size());
        assertEquals("Matrix", result.get(0).getMovie().getTitle());
    }

    @Test
    void testGetFavoriteByIdFound() {
        Favorite favorite = Favorite.builder().id(1L).build();

        when(favoriteRepository.findById(1L)).thenReturn(Optional.of(favorite));

        Favorite result = favoriteService.getFavoriteById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetFavoriteByIdNotFound() {
        when(favoriteRepository.findById(1L)).thenReturn(Optional.empty());

        Favorite result = favoriteService.getFavoriteById(1L);

        assertNull(result);
    }

    @Test
    void testAddFavoriteSuccessfully() {
        User user = User.builder().userId(1L).build();
        Movie movie = Movie.builder().id(10).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(movieRepository.findById(10)).thenReturn(Optional.of(movie));
        when(favoriteRepository.existsByUserUserIdAndMovieId(1L, 10)).thenReturn(false);
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Favorite result = favoriteService.addFavorite(1L, 10);

        assertNotNull(result);
        assertEquals(movie, result.getMovie());
        assertEquals(user, result.getUser());
    }

    @Test
    void testAddFavoriteAlreadyExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().userId(1L).build()));
        when(movieRepository.findById(10)).thenReturn(Optional.of(Movie.builder().id(10).build()));
        when(favoriteRepository.existsByUserUserIdAndMovieId(1L, 10)).thenReturn(true);

        CustomException ex = assertThrows(CustomException.class, () -> {
            favoriteService.addFavorite(1L, 10);
        });

        assertEquals("La película con ID 10 ya está marcada como favorita por el usuario 1.", ex.getMessage());
    }

    @Test
    void testRemoveFavoriteSuccess() {
        when(favoriteRepository.existsByUserUserIdAndMovieId(1L, 10)).thenReturn(true);

        favoriteService.removeFavorite(1L, 10);

        verify(favoriteRepository).deleteByUserUserIdAndMovieId(1L, 10);
    }

    @Test
    void testRemoveFavoriteNotFound() {
        when(favoriteRepository.existsByUserUserIdAndMovieId(1L, 10)).thenReturn(false);

        CustomException ex = assertThrows(CustomException.class, () -> {
            favoriteService.removeFavorite(1L, 10);
        });

        assertEquals("No se encontró favorito para el usuario con ID 1 y película con ID 10.", ex.getMessage());
    }

    @Test
    void testGetFavoritesByEmail() {
        Movie movie = Movie.builder().title("Matrix").build();
        User user = User.builder().email("user@example.com").build();

        Favorite favorite = Favorite.builder()
                .id(1L)
                .user(user)
                .movie(movie)
                .addedDate(LocalDateTime.now())
                .build();

        when(favoriteRepository.findByUserEmail("user@example.com")).thenReturn(List.of(favorite));

        List<FavoriteDTO> result = favoriteService.getFavoritesByEmail("user@example.com");

        assertEquals(1, result.size());
        assertEquals("Matrix", result.get(0).getMovie().getTitle());
    }


}
