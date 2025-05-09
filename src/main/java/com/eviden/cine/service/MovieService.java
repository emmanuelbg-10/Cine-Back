package com.eviden.cine.service;

import com.eviden.cine.dtos.GenreTranslatedDTO;
import com.eviden.cine.dtos.MovieDTO;
import com.eviden.cine.dtos.MovieTranslatedDTO;
import com.eviden.cine.model.*;
import com.eviden.cine.repository.*;
import com.eviden.cine.util.Translator;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ClassificationRepository classificationRepository;
    private final DirectorRepository directorRepository;
    private final ActorRepository actorRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public MovieService(
            MovieRepository movieRepository,
            GenreRepository genreRepository,
            ClassificationRepository classificationRepository,
            DirectorRepository directorRepository,
            ActorRepository actorRepository,
            UserRepository userRepository, CloudinaryService cloudinaryService
    ) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.classificationRepository = classificationRepository;
        this.directorRepository = directorRepository;
        this.actorRepository = actorRepository;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    // ──────────────────────── CRUD ────────────────────────

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Optional<Movie> getMovieById(int id) {
        return movieRepository.findMovieById(id);
    }

    public Optional<Movie> getMovieByTitle(String title) {
        return movieRepository.findMovieByTitle(title);
    }

    public List<Movie> getAvailableMovies() {
        return movieRepository.findByIsAvailableTrue();
    }

    public List<Movie> getComingSoonMovies() {
        return movieRepository.findByIsComingSoonTrue();
    }

    public List<Movie> getMoviesByGenre(String genreName) {
        return movieRepository.findByGenreName(genreName);
    }

    public List<Movie> searchByKeyword(String keyword) {
        // Ignoramos el parámetro 'keyword' aquí, porque el filtrado textual se hará después de traducir
        List<Movie> movies = movieRepository.findAll(); // o ajusta a un método más eficiente si tienes

        return movies.stream()
                .filter(movie -> movie.getEmisiones() != null && !movie.getEmisiones().isEmpty())
                .filter(movie -> movie.getEmisiones().stream()
                        .anyMatch(emision ->
                                emision.getEstado() == Emision.EstadoEmision.ACTIVO &&
                                        tieneAsientosDisponibles(emision)))
                .collect(Collectors.toList());
    }

    @Transactional
    public Movie createMovie(MovieDTO movieDTO, String idiomaOrigen) {
        Movie movie = new Movie();
        // Asignar los campos básicos de la entidad
        movie.setTitle(movieDTO.getTitle());
        movie.setSynopsis(movieDTO.getSynopsis());
        movie.setReleaseDate(movieDTO.getReleaseDate());
        movie.setTime(movieDTO.getTime());
        movie.setUrlImageX(movieDTO.getUrlImageX());
        movie.setUrlImageY(movieDTO.getUrlImageY());
        movie.setUrlTrailer(movieDTO.getUrlTrailer());
        movie.setAvailable(movieDTO.getIsAvailable());
        movie.setComingSoon(movieDTO.getIsComingSoon());

        // Resolver relaciones
        resolveGenre(movieDTO);
        resolveClassification(movieDTO);
        resolveDirector(movieDTO);
        resolveCasting(movieDTO);

        // Aplicar traducciones usando movieDTO.getTitle() y movieDTO.getSynopsis()
        translateMovie(movieDTO, movie, idiomaOrigen);

        movie.setGenre(movieDTO.getGenre());
        movie.setClassification(movieDTO.getClassification());
        movie.setDirector(movieDTO.getDirector());
        movie.setCasting(movieDTO.getCasting());

        return movieRepository.save(movie);
    }

    private static void translateMovie(MovieDTO movieDTO, Movie movie, String idiomaOrigen) {
        if (movieDTO.getTitle() != null && movieDTO.getSynopsis() != null) {
            movie.setTitleEn(idiomaOrigen.equalsIgnoreCase("en")
                    ? movieDTO.getTitle()
                    : Translator.traducir(movieDTO.getTitle(), idiomaOrigen, "en"));
            movie.setTitleFr(idiomaOrigen.equalsIgnoreCase("fr")
                    ? movieDTO.getTitle()
                    : Translator.traducir(movieDTO.getTitle(), idiomaOrigen, "fr"));
            movie.setTitleDe(idiomaOrigen.equalsIgnoreCase("de")
                    ? movieDTO.getTitle()
                    : Translator.traducir(movieDTO.getTitle(), idiomaOrigen, "de"));
            movie.setTitleIt(idiomaOrigen.equalsIgnoreCase("it")
                    ? movieDTO.getTitle()
                    : Translator.traducir(movieDTO.getTitle(), idiomaOrigen, "it"));
            movie.setTitlePt(idiomaOrigen.equalsIgnoreCase("pt")
                    ? movieDTO.getTitle()
                    : Translator.traducir(movieDTO.getTitle(), idiomaOrigen, "pt"));
            movie.setTitle(idiomaOrigen.equalsIgnoreCase("es")
                    ? movieDTO.getTitle()
                    : Translator.traducir(movieDTO.getTitle(), idiomaOrigen, "es"));

            movie.setSynopsisEn(idiomaOrigen.equalsIgnoreCase("en")
                    ? movieDTO.getSynopsis()
                    : Translator.traducir(movieDTO.getSynopsis(), idiomaOrigen, "en"));
            movie.setSynopsisFr(idiomaOrigen.equalsIgnoreCase("fr")
                    ? movieDTO.getSynopsis()
                    : Translator.traducir(movieDTO.getSynopsis(), idiomaOrigen, "fr"));
            movie.setSynopsisDe(idiomaOrigen.equalsIgnoreCase("de")
                    ? movieDTO.getSynopsis()
                    : Translator.traducir(movieDTO.getSynopsis(), idiomaOrigen, "de"));
            movie.setSynopsisIt(idiomaOrigen.equalsIgnoreCase("it")
                    ? movieDTO.getSynopsis()
                    : Translator.traducir(movieDTO.getSynopsis(), idiomaOrigen, "it"));
            movie.setSynopsisPt(idiomaOrigen.equalsIgnoreCase("pt")
                    ? movieDTO.getSynopsis()
                    : Translator.traducir(movieDTO.getSynopsis(), idiomaOrigen, "pt"));
            movie.setSynopsis(idiomaOrigen.equalsIgnoreCase("es")
                    ? movieDTO.getSynopsis()
                    : Translator.traducir(movieDTO.getSynopsis(), idiomaOrigen, "es"));
        }
    }


    @Transactional
    public Movie updateMovie(int id, MovieDTO movieDTO, String idiomaOrigen) {
        return movieRepository.findById(id).map(movie -> {

            // Resolver relaciones antes de asignar
            resolveGenre(movieDTO);
            resolveClassification(movieDTO);
            resolveDirector(movieDTO);
            resolveCasting(movieDTO);

            // Actualizar atributos básicos
            movie.setTitle(movieDTO.getTitle());
            movie.setSynopsis(movieDTO.getSynopsis());
            movie.setReleaseDate(movieDTO.getReleaseDate());
            movie.setTime(movieDTO.getTime());
            movie.setUrlTrailer(movieDTO.getUrlTrailer());
            movie.setAvailable(movieDTO.getIsAvailable());
            movie.setComingSoon(movieDTO.getIsComingSoon());

            // Imagen X: solo actualizar si movieDTO trae una nueva URL
            if (movieDTO.getUrlImageX() != null && !movieDTO.getUrlImageX().isBlank()) {
                movie.setUrlImageX(movieDTO.getUrlImageX());
            } else {
                System.out.println("No se reemplaza urlImageX");
            }

            // Imagen Y: igual que arriba
            if (movieDTO.getUrlImageY() != null && !movieDTO.getUrlImageY().isBlank()) {
                movie.setUrlImageY(movieDTO.getUrlImageY());
            } else {
                System.out.println("No se reemplaza urlImageY");
            }

            // Relaciones
            movie.setGenre(movieDTO.getGenre());
            movie.setClassification(movieDTO.getClassification());
            movie.setDirector(movieDTO.getDirector());
            movie.setCasting(movieDTO.getCasting());

            // Traducciones
            translateMovie(movieDTO, movie, idiomaOrigen);

            return movieRepository.save(movie);
        }).orElse(null);
    }

    @Transactional
    public void deleteMovieById(int id) {
        movieRepository.findById(id).ifPresent(movie -> {
            // Eliminar imagen X si existe
            if (movie.getUrlImageX() != null && !movie.getUrlImageX().isBlank()) {
                String publicIdX = extractPublicIdFromUrl(movie.getUrlImageX());
                if (publicIdX != null) {
                    cloudinaryService.deleteImage(publicIdX);
                }
            }

            // Eliminar imagen Y si existe
            if (movie.getUrlImageY() != null && !movie.getUrlImageY().isBlank()) {
                String publicIdY = extractPublicIdFromUrl(movie.getUrlImageY());
                if (publicIdY != null) {
                    cloudinaryService.deleteImage(publicIdY);
                }
            }

            // Eliminar la película de la base de datos
            movieRepository.deleteById(id);
        });
    }

    private String extractPublicIdFromUrl(String url) {
        try {
            // Obtener la última parte de la URL: el nombre del archivo
            String fileNameWithExtension = url.substring(url.lastIndexOf("/") + 1);
            // Quitar la extensión
            return fileNameWithExtension.split("\\.")[0];
        } catch (Exception e) {
            return null;
        }
    }



    public List<Movie> filterMovies(Specification<Movie> spec) {
        List<Movie> allAvailableMovies = movieRepository.findAll(spec);
        return allAvailableMovies.stream()
                .filter(movie -> movie.getEmisiones() != null && !movie.getEmisiones().isEmpty())
                .filter(movie -> movie.getEmisiones().stream()
                        .anyMatch(emision -> emision.getEstado() == Emision.EstadoEmision.ACTIVO && tieneAsientosDisponibles(emision)))
                .toList();
    }

    // ──────────────────────── Helpers ────────────────────────

    private void resolveClassification(MovieDTO movieDTO) {
        if (movieDTO.getClassification() != null) {
            movieDTO.setClassification(
                    classificationRepository.findByName(movieDTO.getClassification().getName())
                            .orElseGet(() -> classificationRepository.save(
                                    Classification.builder().name(movieDTO.getClassification().getName()).build()))
            );
        }
    }

    private void resolveGenre(MovieDTO movieDTO) {
        if (movieDTO.getGenre() != null) {
            movieDTO.setGenre(
                    genreRepository.findById(movieDTO.getGenre().getId())
                            .orElseGet(() -> genreRepository.save(movieDTO.getGenre()))
            );
        }
    }

    private void resolveCasting(MovieDTO movieDTO) {
        if (movieDTO.getCasting() != null) {
            List<Actor> managedActors = movieDTO.getCasting().stream()
                    .map(actor -> actorRepository.findById(actor.getId())
                            .orElseThrow(() -> new EntityNotFoundException("Actor not found: " + actor.getId())))
                    .collect(Collectors.toList());
            movieDTO.setCasting(managedActors);
        }
    }

    private void resolveDirector(MovieDTO movieDTO) {
        if (movieDTO.getDirector() != null) {
            movieDTO.setDirector(
                    directorRepository.findByName(movieDTO.getDirector().getName())
                            .orElseGet(() -> directorRepository.save(movieDTO.getDirector()))
            );
        }
    }

    // ──────────────────────── Traducción ────────────────────────

    public List<MovieTranslatedDTO> translateMovies(List<Movie> movies, String lang) {
        final String selectedLang = lang != null ? lang.toLowerCase() : "es";

        return movies.stream()
                .map(movie -> {
                    String title = switch (selectedLang) {
                        case "en" -> movie.getTitleEn();
                        case "fr" -> movie.getTitleFr();
                        case "de" -> movie.getTitleDe();
                        case "it" -> movie.getTitleIt();
                        case "pt" -> movie.getTitlePt();
                        default -> movie.getTitle();
                    };
                    String synopsis = switch (selectedLang) {
                        case "en" -> movie.getSynopsisEn();
                        case "fr" -> movie.getSynopsisFr();
                        case "de" -> movie.getSynopsisDe();
                        case "it" -> movie.getSynopsisIt();
                        case "pt" -> movie.getSynopsisPt();
                        default -> movie.getSynopsis();
                    };

                    GenreTranslatedDTO genreDTO = null;
                    if (movie.getGenre() != null) {
                        genreDTO = new GenreTranslatedDTO(
                                movie.getGenre().getId(),
                                movie.getGenre().getTranslatedName(selectedLang)
                        );
                    }

                    return new MovieTranslatedDTO(
                            movie.getId(),
                            title,
                            synopsis,
                            movie.getUrlImageX(),
                            movie.getUrlImageY(),
                            movie.getUrlTrailer(),
                            genreDTO,
                            movie.getClassification(),
                            movie.getRating(),
                            movie.getReleaseDate(),
                            movie.getTime(),
                            movie.getDirector(),
                            movie.isAvailable(),
                            movie.isComingSoon(),
                            movie.getReviews(),
                            movie.getFavorites(),
                            movie.getEmisiones(),
                            movie.getCasting()
                    );
                })
                .toList();
    }

    public MovieTranslatedDTO translateMovie(Movie movie, String lang) {
        final String selectedLang = lang != null ? lang.toLowerCase() : "es";

        String title = switch (selectedLang) {
            case "en" -> movie.getTitleEn();
            case "fr" -> movie.getTitleFr();
            case "de" -> movie.getTitleDe();
            case "it" -> movie.getTitleIt();
            case "pt" -> movie.getTitlePt();
            default -> movie.getTitle();
        };

        String synopsis = switch (selectedLang) {
            case "en" -> movie.getSynopsisEn();
            case "fr" -> movie.getSynopsisFr();
            case "de" -> movie.getSynopsisDe();
            case "it" -> movie.getSynopsisIt();
            case "pt" -> movie.getSynopsisPt();
            default -> movie.getSynopsis();
        };

        GenreTranslatedDTO genreDTO = null;
        if (movie.getGenre() != null) {
            genreDTO = new GenreTranslatedDTO(
                    movie.getGenre().getId(),
                    movie.getGenre().getTranslatedName(selectedLang)
            );
        }

        return new MovieTranslatedDTO(
                movie.getId(),
                title,
                synopsis,
                movie.getUrlImageX(),
                movie.getUrlImageY(),
                movie.getUrlTrailer(),
                genreDTO,
                movie.getClassification(),
                movie.getRating(),
                movie.getReleaseDate(),
                movie.getTime(),
                movie.getDirector(),
                movie.isAvailable(),
                movie.isComingSoon(),
                movie.getReviews(),
                movie.getFavorites(),
                movie.getEmisiones(),
                movie.getCasting()
        );
    }


    public String getTituloTraducido(Movie movie, String lang) {
        if (movie == null) return null;
        final String selectedLang = lang != null ? lang.toLowerCase() : "es";

        return switch (selectedLang) {
            case "en" -> movie.getTitleEn();
            case "fr" -> movie.getTitleFr();
            case "de" -> movie.getTitleDe();
            case "it" -> movie.getTitleIt();
            case "pt" -> movie.getTitlePt();
            default -> movie.getTitle(); // Español por defecto
        };
    }






    public List<Movie> getMoviesForBillboard() {
        List<Movie> allAvailableMovies = movieRepository.findByIsAvailableTrue();

        return allAvailableMovies.stream()
                .filter(movie -> movie.getEmisiones() != null && !movie.getEmisiones().isEmpty())
                .filter(movie -> movie.getEmisiones().stream()
                        .anyMatch(emision -> emision.getEstado() == Emision.EstadoEmision.ACTIVO && tieneAsientosDisponibles(emision)))
                .toList();
    }

    private boolean tieneAsientosDisponibles(Emision emision) {
        if (emision.getRoom() == null || emision.getRoom().getAsientos() == null) {
            return false;
        }
        //se comprueba que haya minimo un sasiento
        return emision.getRoom().getAsientos().stream()
                .anyMatch(Asiento::isDisponible);
    }

    public String resolveLanguage(String email, String lang) {
        if (lang != null) {
            return lang; // Priorizar el encabezado de lenguaje
        } else if (email != null) {
            return userRepository.findByEmail(email)
                    .map(User::getPreferredLanguage)
                    .orElse("es");
        } else {
            return "es"; // Valor por defecto
        }
    }

}
