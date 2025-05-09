package com.eviden.cine.mapper;

import com.eviden.cine.dtos.*;
import com.eviden.cine.model.Actor;
import com.eviden.cine.model.Movie;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MovieMapper {

    public static MovieResponseDTO mapToMovieResponse(FavoriteDTO favorite) {
        Movie movie = favorite.getMovie();

        // Obtener el año, mes y día de la fecha de estreno correctamente usando LocalDate
        LocalDate releaseDate = movie.getReleaseDate();
        int year = releaseDate.getYear();  // Obtiene el año
        int month = releaseDate.getMonthValue();  // Obtiene el mes (1-12)
        int day = releaseDate.getDayOfMonth();  // Obtiene el día del mes (1-31)

        // Construir el MovieResponseDTO con los valores mapeados
        return MovieResponseDTO.builder()
                .id((long) movie.getId())
                .title(movie.getTitle())
                .synopsis(movie.getSynopsis())
                .urlImageX(movie.getUrlImageX())
                .urlImageY(movie.getUrlImageY())
                .urlTrailer(movie.getUrlTrailer())
                .genre(new GenreDTO((long) movie.getGenre().getId(), movie.getGenre().getName()))
                .classification(new ClasificationDTO((long) movie.getClassification().getId(), movie.getClassification().getName()))
                .rating(movie.getRating())
                // Usamos el formato correcto para la fecha
                .releaseDate(movie.getReleaseDate())
                .time(movie.getTime())
                .director(movie.getDirector().getName())
                .isAvailable(movie.isAvailable())
                .isComingSoon(movie.isComingSoon())
                .casting(movie.getCasting().stream().map(Actor::getName).collect(Collectors.toList()))
                .favorites(List.of(
                        FavoriteResponseDTO.builder()
                                .id(favorite.getFavoriteId())
                                .addedDate(favorite.getAddedDate().toLocalDate().toString())
                                .build()
                ))
                .review(new ArrayList<>()) // Placeholder para reviews
                .emisiones(new ArrayList<>()) // Placeholder para emisiones
                .build();
    }
}
