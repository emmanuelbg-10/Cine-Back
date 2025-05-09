// Archivo: src/test/java/com/eviden/cine/movieFilter/MoviesSpecificationsTest.java
package com.eviden.cine.movieFilter;

import com.eviden.cine.model.Genre;
import com.eviden.cine.model.Movie;
import com.eviden.cine.movie_filter.MoviesSpecifications;
import com.eviden.cine.repository.GenreRepository;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MoviesSpecificationsTest {

    private Root<Movie> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder cb;

    @BeforeEach
    void setUp() {
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        cb = mock(CriteriaBuilder.class);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    void testHasGenreTranslated_Found() {
        GenreRepository genreRepository = mock(GenreRepository.class);
        Genre genre = mock(Genre.class);
        when(genre.getTranslatedName("es")).thenReturn("Acción");
        when(genreRepository.findByName("Acción")).thenReturn(Optional.of(genre));

        MoviesSpecifications specs = new MoviesSpecifications(genreRepository);

        Join<Object, Object> genreJoin = (Join) mock(Join.class);
        Path<Object> namePath = (Path) mock(Path.class);
        when(root.join("genre")).thenReturn(genreJoin);
        when(genreJoin.get("name")).thenReturn(namePath);
        Predicate expectedPredicate = mock(Predicate.class);
        when(cb.equal(namePath, "Acción")).thenReturn(expectedPredicate);

        Specification<Movie> spec = specs.hasGenreTranslated("Acción", "es");
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        assertEquals(expectedPredicate, result);
        verify(genreRepository).findByName("Acción");
        verify(cb).equal(namePath, "Acción");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    void testHasGenreTranslated_NotFound() {
        GenreRepository genreRepository = mock(GenreRepository.class);
        when(genreRepository.findByName("Drama")).thenReturn(Optional.empty());

        MoviesSpecifications specs = new MoviesSpecifications(genreRepository);

        Join<Object, Object> genreJoin = (Join) mock(Join.class);
        Path<Object> namePath = (Path) mock(Path.class);
        when(root.join("genre")).thenReturn(genreJoin);
        when(genreJoin.get("name")).thenReturn(namePath);
        Predicate expectedPredicate = mock(Predicate.class);
        when(cb.equal(namePath, "Drama")).thenReturn(expectedPredicate);

        Specification<Movie> spec = specs.hasGenreTranslated("Drama", "es");
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        assertEquals(expectedPredicate, result);
        verify(genreRepository).findByName("Drama");
        verify(cb).equal(namePath, "Drama");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    void testHasClassification() {
        Join<Object, Object> classificationJoin = (Join) mock(Join.class);
        Path<Object> namePath = (Path) mock(Path.class);
        Predicate expectedPredicate = mock(Predicate.class);
        when(root.join("classification")).thenReturn(classificationJoin);
        when(classificationJoin.get("name")).thenReturn(namePath);
        when(cb.equal(namePath, "PG-13")).thenReturn(expectedPredicate);

        Specification<Movie> spec = MoviesSpecifications.hasClassification("PG-13");
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        assertEquals(expectedPredicate, result);
        verify(cb).equal(namePath, "PG-13");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    void testReleasedInMonth() {
        Path<Date> datePath = (Path<Date>) mock(Path.class);
        Predicate expectedPredicate = mock(Predicate.class);
        when(root.get("releaseDate")).thenReturn((Path)datePath);
        when(cb.between(eq(datePath), any(Date.class), any(Date.class))).thenReturn(expectedPredicate);

        int year = 2025;
        int month = 4;
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar.getTime();
        calendar.set(year, month - 1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endDate = calendar.getTime();

        Specification<Movie> spec = MoviesSpecifications.releasedInMonth(year, month);
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).between(eq(datePath), any(Date.class), any(Date.class));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    void testIsPopular() {
        Path<Integer> idPath = (Path<Integer>) mock(Path.class);
        Predicate expectedPredicate = mock(Predicate.class);
        when(root.get("id")).thenReturn((Path)idPath);
        when(cb.greaterThan(idPath, 5)).thenReturn(expectedPredicate);

        Specification<Movie> spec = MoviesSpecifications.isPopular();
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        assertEquals(expectedPredicate, result);
        verify(cb).greaterThan(idPath, 5);
    }

    @Test
    void testGetGenreByName_Found() {
        GenreRepository genreRepository = mock(GenreRepository.class);
        Genre genre = mock(Genre.class);
        when(genreRepository.findByName("Comedia")).thenReturn(Optional.of(genre));

        MoviesSpecifications specs = new MoviesSpecifications(genreRepository);
        Genre result = specs.getGenreByName("Comedia");

        assertNotNull(result);
        assertEquals(genre, result);
        verify(genreRepository).findByName("Comedia");
    }

    @Test
    void testGetGenreByName_NotFound() {
        GenreRepository genreRepository = mock(GenreRepository.class);
        when(genreRepository.findByName("Terror")).thenReturn(Optional.empty());

        MoviesSpecifications specs = new MoviesSpecifications(genreRepository);
        Genre result = specs.getGenreByName("Terror");

        assertNull(result);
        verify(genreRepository).findByName("Terror");
    }
}