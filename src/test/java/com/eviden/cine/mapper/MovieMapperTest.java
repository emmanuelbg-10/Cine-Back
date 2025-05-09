package com.eviden.cine.mapper;

import com.eviden.cine.dtos.FavoriteDTO;
import com.eviden.cine.dtos.MovieResponseDTO;
import com.eviden.cine.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MovieMapperTest {

    @Test
    public void testMapToMovieResponse() {
        // Setup Genre
        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("Action");

        // Setup Classification
        Classification classification = new Classification();
        classification.setId(2);
        classification.setName("PG-13");

        // Setup Director
        Director director = new Director();
        director.setId(3);
        director.setName("John Doe");

        // Setup Actors
        Actor actor1 = new Actor();
        actor1.setId(10);
        actor1.setName("Actor One");

        Actor actor2 = new Actor();
        actor2.setId(11);
        actor2.setName("Actor Two");

        // Setup Movie
        Movie movie = new Movie();
        movie.setId(100);
        movie.setTitle("Test Movie");
        movie.setSynopsis("Test Synopsis");
        movie.setUrlImageX("http://imageX.com");
        movie.setUrlImageY("http://imageY.com");
        movie.setUrlTrailer("http://trailer.com");
        movie.setGenre(genre);
        movie.setClassification(classification);
        movie.setRating(4.5);
        movie.setReleaseDate(LocalDate.of(2023, 5, 15));
        movie.setTime(120);
        movie.setDirector(director);
        movie.setAvailable(true);
        movie.setComingSoon(false);
        movie.setCasting(List.of(actor1, actor2));

        // Setup FavoriteDTO
        FavoriteDTO favorite = new FavoriteDTO();
        favorite.setFavoriteId(999L);
        favorite.setAddedDate(LocalDateTime.of(2023, 6, 1, 12, 0));
        favorite.setMovie(movie);

        // Act
        MovieResponseDTO dto = MovieMapper.mapToMovieResponse(favorite);

        // Assert
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getTitle()).isEqualTo("Test Movie");
        assertThat(dto.getSynopsis()).isEqualTo("Test Synopsis");
        assertThat(dto.getUrlImageX()).isEqualTo("http://imageX.com");
        assertThat(dto.getUrlImageY()).isEqualTo("http://imageY.com");
        assertThat(dto.getUrlTrailer()).isEqualTo("http://trailer.com");
        assertThat(dto.getGenre().getId()).isEqualTo(1L);
        assertThat(dto.getGenre().getName()).isEqualTo("Action");
        assertThat(dto.getClassification().getId()).isEqualTo(2L);
        assertThat(dto.getClassification().getName()).isEqualTo("PG-13");
        assertThat(dto.getRating()).isEqualTo(4.5);
        assertThat(dto.getReleaseDate()).isEqualTo(LocalDate.of(2023, 5, 15));
        assertThat(dto.getTime()).isEqualTo(120);
        assertThat(dto.getDirector()).isEqualTo("John Doe");
        assertThat(dto.getIsAvailable()).isTrue();
        assertThat(dto.getIsComingSoon()).isFalse();
        assertThat(dto.getCasting()).containsExactly("Actor One", "Actor Two");

        assertThat(dto.getFavorites()).hasSize(1);
        assertThat(dto.getFavorites().getFirst().getId()).isEqualTo(999L);
        assertThat(dto.getFavorites().getFirst().getAddedDate()).isEqualTo("2023-06-01");

        assertThat(dto.getReview()).isEmpty();
        assertThat(dto.getEmisiones()).isEmpty();
    }
}
