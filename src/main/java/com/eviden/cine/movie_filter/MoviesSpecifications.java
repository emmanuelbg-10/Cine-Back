package com.eviden.cine.movie_filter;

import com.eviden.cine.model.Genre;
import com.eviden.cine.model.Movie;
import com.eviden.cine.repository.GenreRepository;
import org.springframework.data.jpa.domain.Specification;

import java.util.Calendar;
import java.util.Date;

public final class MoviesSpecifications {

    private final GenreRepository genreRepository; // Instancia del repositorio

    // Constructor para inyectar el repositorio
    public MoviesSpecifications(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    // Método de ejemplo para obtener el género traducido y realizar la consulta
    public Specification<Movie> hasGenreTranslated(String genreName, String language) {
        return (root, query, cb) -> {
            String translatedGenreName = translateGenreName(genreName, language);
            return cb.equal(root.join("genre").get("name"), translatedGenreName);
        };
    }

    private String translateGenreName(String genreName, String language) {
        Genre genre = getGenreByName(genreName);
        if (genre != null) {
            return genre.getTranslatedName(language);
        }
        return genreName; // Si no se traduce, se usa el nombre original
    }

    public Genre getGenreByName(String genreName) {
        return genreRepository.findByName(genreName).orElse(null); // Busca el género por nombre
    }

    public static Specification<Movie> hasClassification(String classificationName) {
        return (root, query, cb) -> cb.equal(root.join("classification").get("name"), classificationName);
    }

    public static Specification<Movie> releasedInMonth(int year, int month) {
        return (root, query, cb) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month - 1, 1);
            Date startDate = calendar.getTime();

            calendar.set(year, month - 1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date endDate = calendar.getTime();

            return cb.between(root.get("releaseDate"), startDate, endDate);
        };
    }

    public static Specification<Movie> isPopular() {
        return (root, query, cb) -> cb.greaterThan(root.get("id"), 5); // simulación: popularidad
    }

    public static Specification<Movie> hasRegion(String regionName) {
        return (root, query, cb) -> {
            return cb.equal(
                    cb.lower(root.join("emisiones")      // Movie → Emision
                            .join("room")              // Emision → Room
                            .join("region")            // Room   → Region
                            .get("name")),
                    regionName.toLowerCase()
            );
        };
    }
}
