package com.eviden.cine.service;

import com.eviden.cine.dtos.MovieDTO;
import com.eviden.cine.dtos.MovieTranslatedDTO;
import com.eviden.cine.model.*;
import com.eviden.cine.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class MovieServiceTest {

    @Mock private MovieRepository movieRepository;
    @Mock private GenreRepository genreRepository;
    @Mock private ClassificationRepository classificationRepository;
    @Mock private DirectorRepository directorRepository;
    @Mock private ActorRepository actorRepository;
    @Mock private UserRepository userRepository;
    @Mock private CloudinaryService cloudinaryService;

    @InjectMocks
    private MovieService movieService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllMovies_returnsList() {
        when(movieRepository.findAll()).thenReturn(List.of(new Movie()));
        List<Movie> result = movieService.getAllMovies();
        assertEquals(1, result.size());
    }

    @Test
    void getAvailableMovies_filtersCorrectly() {
        Movie m1 = new Movie(); m1.setAvailable(true);
        when(movieRepository.findByIsAvailableTrue()).thenReturn(List.of(m1));
        List<Movie> result = movieService.getAvailableMovies();
        assertEquals(1, result.size());
    }

    @Test
    void getComingSoonMovies_filtersCorrectly() {
        Movie m1 = new Movie(); m1.setComingSoon(true);
        when(movieRepository.findByIsComingSoonTrue()).thenReturn(List.of(m1));
        List<Movie> result = movieService.getComingSoonMovies();
        assertEquals(1, result.size());
    }

    @Test
    void getMovieById_found() {
        Movie movie = new Movie();
        when(movieRepository.findMovieById(1)).thenReturn(Optional.of(movie));
        assertTrue(movieService.getMovieById(1).isPresent());
    }

    @Test
    void getMovieByTitle_found() {
        Movie movie = new Movie();
        when(movieRepository.findMovieByTitle("Matrix")).thenReturn(Optional.of(movie));
        assertTrue(movieService.getMovieByTitle("Matrix").isPresent());
    }

    @Test
    void getMoviesByGenre_ok() {
        when(movieRepository.findByGenreName("Action")).thenReturn(List.of(new Movie()));
        List<Movie> result = movieService.getMoviesByGenre("Action");
        assertEquals(1, result.size());
    }

    // src/test/java/com/eviden/cine/service/MovieServiceTest.java
    @Test
    void searchByKeyword_ok() {
        // Crear un objeto Emision con condiciones válidas
        Emision emision = new Emision();
        emision.setEstado(Emision.EstadoEmision.ACTIVO);
        Room room = new Room();
        Asiento asiento = new Asiento();
        asiento.setDisponible(true);
        room.setAsientos(Collections.singletonList(asiento));
        emision.setRoom(room);

        Movie movie = new Movie();
        movie.setEmisiones(Collections.singletonList(emision));

        when(movieRepository.findAll()).thenReturn(List.of(movie));

        List<Movie> result = movieService.searchByKeyword("test");
        assertEquals(1, result.size());
    }

    @Test
    void createMovie_ok() {
        MovieDTO dto = new MovieDTO();
        dto.setTitle("Test");
        dto.setSynopsis("Synopsis");
        dto.setReleaseDate(LocalDate.now());
        dto.setTime(120);
        dto.setUrlImageX("http://img/x");
        dto.setUrlImageY("http://img/y");
        dto.setUrlTrailer("http://trailer");
        dto.setIsAvailable(true); // ⚠️ Esto es obligatorio
        dto.setIsComingSoon(false);

        Genre genre = Genre.builder().id(1).name("Action").build();
        Classification classification = Classification.builder().name("PG").build();
        Director director = Director.builder().name("Nolan").build();
        Actor actor = Actor.builder().id(1).name("Actor").build();

        dto.setGenre(genre);
        dto.setClassification(classification);
        dto.setDirector(director);
        dto.setCasting(List.of(actor));

        when(genreRepository.findById(1)).thenReturn(Optional.of(genre));
        when(classificationRepository.findByName("PG")).thenReturn(Optional.of(classification));
        when(directorRepository.findByName("Nolan")).thenReturn(Optional.of(director));
        when(actorRepository.findById(1)).thenReturn(Optional.of(actor));
        when(movieRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Movie movie = movieService.createMovie(dto, "es");
        assertEquals("Test", movie.getTitle());
    }

    @Test
    void updateMovie_ok() {
        MovieDTO dto = new MovieDTO();
        dto.setTitle("Updated");
        dto.setSynopsis("New synopsis");
        dto.setReleaseDate(LocalDate.now());
        dto.setTime(100);
        dto.setUrlImageX("http://newX");
        dto.setUrlImageY("http://newY");
        dto.setUrlTrailer("http://newTrailer");
        dto.setIsAvailable(true);
        dto.setIsComingSoon(true);

        Genre genre = Genre.builder().id(1).name("Drama").build();
        Classification classification = Classification.builder().name("G").build();
        Director director = Director.builder().name("Spielberg").build();
        Actor actor = Actor.builder().id(2).name("Actor2").build();

        dto.setGenre(genre);
        dto.setClassification(classification);
        dto.setDirector(director);
        dto.setCasting(List.of(actor));

        Movie existing = new Movie();
        existing.setId(1);

        when(movieRepository.findById(1)).thenReturn(Optional.of(existing));
        when(genreRepository.findById(1)).thenReturn(Optional.of(genre));
        when(classificationRepository.findByName("G")).thenReturn(Optional.of(classification));
        when(directorRepository.findByName("Spielberg")).thenReturn(Optional.of(director));
        when(actorRepository.findById(2)).thenReturn(Optional.of(actor));
        when(movieRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Movie updated = movieService.updateMovie(1, dto, "es");
        assertEquals("Updated", updated.getTitle());
    }

    @Test
    void deleteMovieById_ok() {
        Movie movie = new Movie();
        movie.setUrlImageX("http://cloudinary.com/x.jpg");
        movie.setUrlImageY("http://cloudinary.com/y.jpg");

        when(movieRepository.findById(1)).thenReturn(Optional.of(movie));

        movieService.deleteMovieById(1);

        verify(cloudinaryService).deleteImage("x");
        verify(cloudinaryService).deleteImage("y");
        verify(movieRepository).deleteById(1);
    }

    @Test
    void translateMovie_returnsCorrectTranslation() {
        // mockeamos el genre
        Genre genre = mock(Genre.class);
        when(genre.getId()).thenReturn(1);
        when(genre.getTranslatedName("en")).thenReturn("Drama");

        Movie movie = new Movie();
        movie.setTitleEn("TitleEN");
        movie.setSynopsisEn("SynopsisEN");
        movie.setTitle("Título");
        movie.setSynopsis("Sinopsis");
        movie.setGenre(genre);  // importante

        MovieTranslatedDTO dto = movieService.translateMovie(movie, "en");

        assertEquals("TitleEN", dto.getTitle());
        assertEquals("SynopsisEN", dto.getSynopsis());
        assertEquals("Drama", dto.getGenre().getName());
    }


    @Test
    void getMoviesForBillboard_returnsValidMovies() {
        Movie movie = new Movie();
        movie.setAvailable(true);

        Asiento asiento = new Asiento(); asiento.setDisponible(true);
        Room room = new Room(); room.setAsientos(List.of(asiento));
        Emision emision = new Emision(); emision.setEstado(Emision.EstadoEmision.ACTIVO); emision.setRoom(room);

        movie.setEmisiones(List.of(emision));
        when(movieRepository.findByIsAvailableTrue()).thenReturn(List.of(movie));

        List<Movie> result = movieService.getMoviesForBillboard();
        assertEquals(1, result.size());
    }

    @Test
    void filterMovies_ok() {
        Movie movie = new Movie();

        Asiento asiento = new Asiento(); asiento.setDisponible(true);
        Room room = new Room(); room.setAsientos(List.of(asiento));
        Emision emision = new Emision(); emision.setEstado(Emision.EstadoEmision.ACTIVO); emision.setRoom(room);

        movie.setEmisiones(List.of(emision));

        Specification<Movie> spec = (root, query, cb) -> null;
        when(movieRepository.findAll(spec)).thenReturn(List.of(movie));

        List<Movie> result = movieService.filterMovies(spec);
        assertEquals(1, result.size());
    }

    @Test
    void resolveLanguage_emailPreferred() {
        User user = new User(); user.setPreferredLanguage("fr");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        String lang = movieService.resolveLanguage("user@example.com", null);
        assertEquals("fr", lang);
    }

    @Test
    void resolveLanguage_fromHeader() {
        String lang = movieService.resolveLanguage(null, "it");
        assertEquals("it", lang);
    }

    @Test
    void resolveLanguage_defaultToSpanish() {
        String lang = movieService.resolveLanguage(null, null);
        assertEquals("es", lang);
    }
}
