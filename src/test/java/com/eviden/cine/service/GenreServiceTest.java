package com.eviden.cine.service;

import com.eviden.cine.dtos.GenreTranslatedDTO;
import com.eviden.cine.model.Genre;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.GenreRepository;
import com.eviden.cine.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenreServiceTest {
    @Mock
    private GenreRepository genreRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GenreService genreService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllGenres(){
        Genre genre = new Genre();
        genre.setName("Sci-Fi");
        Genre genre2 = new Genre();
        genre2.setName("Action");

       when(genreRepository.findAll()).thenReturn(List.of(genre, genre2));

       List<Genre> genres = genreService.getAllGenres();

       assertEquals(2, genres.size());
       assertEquals("Sci-Fi", genres.get(0).getName());
       assertEquals("Action", genres.get(1).getName());
    }

    @Test
    void testCreateGenre() {
        Genre genre = new Genre();
        genre.setName("Horror");

        when(genreRepository.save(genre)).thenReturn(genre);

        Genre createdGenre = genreService.createGenre(genre);

        assertNotNull(createdGenre);
        assertEquals("Horror", createdGenre.getName());
    }

    @Test
    void testGetGenreByIdNotFound() {
        when(genreRepository.findById(99)).thenReturn(Optional.empty());

        Genre genre = genreService.getGenreById(99);

        assertNull(genre);
    }

    @Test
    void testDeleteGenreById() {
        genreService.deleteGenreById(1);

        verify(genreRepository).deleteById(1);
    }

    @Test
    void testGetGenresForUserLanguage() {
        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("Acción");
        genre.setNameEn("Action");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPreferredLanguage("en");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(genreRepository.findAll()).thenReturn(List.of(genre));

        List<GenreTranslatedDTO> result = genreService.getGenresForUserLanguage("test@example.com");

        assertEquals(1, result.size());
        assertEquals("Action", result.getFirst().getName());
    }

    @Test
    void testGetGenresByUserOrHeader_emailPreference() {
        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("Acción");
        genre.setNameEn("Action");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPreferredLanguage("en");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(genreRepository.findAll()).thenReturn(List.of(genre));

        List<GenreTranslatedDTO> result = genreService.getGenresByUserOrHeader("test@example.com", null);

        assertEquals(1, result.size());
        assertEquals("Action", result.getFirst().getName());
    }

    @Test
    void testGetGenresByUserOrHeader_headerPreference() {
        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("Acción");
        genre.setNameFr("ActionFR");

        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());
        when(genreRepository.findAll()).thenReturn(List.of(genre));

        List<GenreTranslatedDTO> result = genreService.getGenresByUserOrHeader(null, "fr");

        assertEquals(1, result.size());
        assertEquals("ActionFR", result.getFirst().getName());
    }
}
