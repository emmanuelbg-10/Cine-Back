package com.eviden.cine.controller;

import com.eviden.cine.dtos.MovieDTO;
import com.eviden.cine.dtos.MovieTranslatedDTO;
import com.eviden.cine.model.Genre;
import com.eviden.cine.model.Movie;
import com.eviden.cine.movie_filter.MoviesSpecifications;
import com.eviden.cine.service.CloudinaryService;
import com.eviden.cine.service.MovieService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/movies")
@Tag(name = "Movies", description = "Operaciones relacionadas con películas")
public class MovieController {

    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);


    private final MovieService movieService;
    private final CloudinaryService cloudinaryService;

    public MovieController(MovieService movieService, CloudinaryService cloudinaryService) {
        this.movieService = movieService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping("/all")
    @Operation(
            summary = "Obtener todas las películas traducidas",
            description = "Devuelve una lista completa de películas con los textos traducidos al idioma solicitado"
    )
    public ResponseEntity<List<MovieTranslatedDTO>> getAllMoviesTranslated(
            Authentication authentication,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        String email = authentication != null ? authentication.getName() : null;
        String language = movieService.resolveLanguage(email, acceptLanguage);

        List<Movie> allMovies = movieService.getAllMovies();
        List<MovieTranslatedDTO> translatedMovies = movieService.translateMovies(allMovies, language);
        return ResponseEntity.ok(translatedMovies);
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Obtener película traducida por ID", description = "Devuelve los datos de una película traducidos al idioma solicitado")
    public ResponseEntity<MovieTranslatedDTO> getMovieByIdTranslated(
            Authentication authentication,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            @Parameter(description = "ID de la película") @PathVariable int id
    ) {
        String email = authentication != null ? authentication.getName() : null;
        String language = movieService.resolveLanguage(email, acceptLanguage);

        return movieService.getMovieById(id)
                .map(movie -> {
                    List<MovieTranslatedDTO> translatedList = movieService.translateMovies(List.of(movie), language);
                    return ResponseEntity.ok(translatedList.getFirst());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/title/{title}")
    @Operation(summary = "Buscar película por título", description = "Obtiene una película a partir de su título")
    public ResponseEntity<Movie> getMovieByTitle(@Parameter(description = "Título de la película") @PathVariable String title) {
        return movieService.getMovieByTitle(title)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar películas", description = "Busca por título, director o actor")
    public ResponseEntity<List<MovieTranslatedDTO>> searchMovies(
            @RequestParam(name = "q") String query,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            Authentication authentication) {

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        String userEmail = (authentication != null) ? authentication.getName() : null;
        String resolvedLanguage = movieService.resolveLanguage(userEmail, acceptLanguage);

        // Buscar solo películas disponibles, sin filtrado por texto
        List<Movie> availableMovies = movieService.searchByKeyword(null);

        // Traducir películas al idioma correspondiente
        List<MovieTranslatedDTO> translatedMovies = movieService.translateMovies(availableMovies, resolvedLanguage);

        // Ahora sí, hacer búsqueda textual con el idioma correcto
        String lowerCaseQuery = query.toLowerCase();
        List<MovieTranslatedDTO> filteredMovies = translatedMovies.stream()
                .filter(movie ->
                        movie.getTitle() != null && movie.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                                movie.getDirector() != null && movie.getDirector().getName() != null &&
                                        movie.getDirector().getName().toLowerCase().contains(lowerCaseQuery) ||
                                movie.getCasting() != null && movie.getCasting().stream()
                                        .anyMatch(actor -> actor.getName() != null &&
                                                actor.getName().toLowerCase().contains(lowerCaseQuery))
                )
                .collect(Collectors.toList());

        return ResponseEntity.ok(filteredMovies);
    }

    @GetMapping("/genre/{genreName}")
    @Operation(summary = "Buscar películas por género", description = "Devuelve una lista de películas según el género indicado")
    public ResponseEntity<List<Movie>> getMoviesByGenre(@PathVariable String genreName) {
        return ResponseEntity.ok(movieService.getMoviesByGenre(genreName));
    }

    @GetMapping("/available")
    @Operation(summary = "Películas disponibles", description = "Devuelve las películas actualmente disponibles en cartelera")
    public ResponseEntity<List<Movie>> getAvailableMovies() {
        return ResponseEntity.ok(movieService.getAvailableMovies());
    }

    @GetMapping("/coming-soon")
    @Operation(summary = "Películas próximas", description = "Devuelve las películas que se estrenarán próximamente")
    public ResponseEntity<List<MovieTranslatedDTO>> getComingSoonMovies(
            Authentication authentication,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
            ) {

        String email = authentication != null ? authentication.getName() : null;
        String language = movieService.resolveLanguage(email, acceptLanguage);

        List<MovieTranslatedDTO> traslated = movieService.translateMovies(movieService.getComingSoonMovies(), language);

        return ResponseEntity.ok(traslated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    @Operation(summary = "Crea una nueva película")
    public ResponseEntity<Movie> createMovie(@RequestParam("imageX") MultipartFile imageX,
                                             @RequestParam("imageY") MultipartFile imageY,
                                             @RequestParam("movieDTO") String movieDTO,
    @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) throws IOException {

        // Crear un ObjectMapper para manejar LocalDate
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Registrar el módulo para manejar LocalDate

        // Convertir el String movieDTO en un objeto MovieDTO
        MovieDTO movieDto = objectMapper.readValue(movieDTO, MovieDTO.class); // Aquí hacemos el parseo del JSON
        System.out.println(movieDto.toString());
        // Subir imágenes a Cloudinary
        if (imageX.isEmpty()) {
            System.out.println("El archivo imageX está vacío.");
        }
        if (imageY.isEmpty()) {
            System.out.println("El archivo imageY está vacío.");
        }

        String urlImageX = cloudinaryService.uploadImage(imageX, 1200, 600); // Cargar la imagen X
        String urlImageY = cloudinaryService.uploadImage(imageY, 400, 600); // Cargar la imagen Y

        System.out.println("URL de la imagen X: " + urlImageX);
        System.out.println("URL de la imagen Y: " + urlImageY);
        // Asignar las URL de las imágenes subidas al DTO
        movieDto.setUrlImageX(urlImageX);
        movieDto.setUrlImageY(urlImageY);

        // Crear la película a través del servicio, que se encarga de resolver las relaciones
        Movie created = movieService.createMovie(movieDto, acceptLanguage);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{id}")
    @Operation(summary = "Actualiza una película existente")
    public ResponseEntity<?> updateMovie(@PathVariable Integer id,
                                         @RequestParam(value = "imageX", required = false) MultipartFile imageX,
                                         @RequestParam(value = "imageY", required = false) MultipartFile imageY,
                                         @RequestParam("movieDTO") String movieDTO,@RequestHeader(value = "Accept-Language", required = false) String acceptLanguage)
     {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            MovieDTO movieDto = objectMapper.readValue(movieDTO, MovieDTO.class);

            // Obtener película actual desde la base de datos
            Optional<Movie> movieOptional = movieService.getMovieById(id);
            if (movieOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Película no encontrada");
            }
            Movie existingMovie = movieOptional.get();

            // Imagen X
            if (imageX != null && !imageX.isEmpty()) {
                if (existingMovie.getUrlImageX() != null && !existingMovie.getUrlImageX().isBlank()) {
                    String oldPublicId = extractPublicIdFromUrl(existingMovie.getUrlImageX());
                    if (oldPublicId != null) {
                        cloudinaryService.deleteImage(oldPublicId);
                    }
                }
                String urlImageX = cloudinaryService.uploadImage(imageX, 1200, 600);
                movieDto.setUrlImageX(urlImageX);
            }

            // Imagen Y
            if (imageY != null && !imageY.isEmpty()) {
                if (existingMovie.getUrlImageY() != null && !existingMovie.getUrlImageY().isBlank()) {
                    String oldPublicId = extractPublicIdFromUrl(existingMovie.getUrlImageY());
                    if (oldPublicId != null) {
                        cloudinaryService.deleteImage(oldPublicId);
                    }
                }
                String urlImageY = cloudinaryService.uploadImage(imageY, 400, 600);
                movieDto.setUrlImageY(urlImageY);
            }

            // Actualizar
            Movie updatedMovie = movieService.updateMovie(id, movieDto, acceptLanguage);
            return ResponseEntity.ok(updatedMovie);

        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al procesar el JSON: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error al actualizar película", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: " + e.getMessage());
        }
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


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Eliminar película", description = "Elimina una película por su ID")
    public ResponseEntity<Void> deleteMovie(@PathVariable int id) {
        movieService.deleteMovieById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    @Operation(summary = "Filtrar películas traducidas", description = "Filtra películas y devuelve los resultados traducidos")
    public ResponseEntity<List<MovieTranslatedDTO>> filterMoviesWithTranslation(
            Authentication authentication,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String classification,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false, defaultValue = "false") boolean popular,
            @RequestParam(required = false) String region
    ) {
        String email = authentication != null ? authentication.getName() : null;
        String language = movieService.resolveLanguage(email, acceptLanguage);

        Specification<Movie> spec = Specification.where(null);
        if (genre != null) spec = spec.and(hasGenreTranslate(genre, language));  // Aquí pasamos el idioma
        if (classification != null) spec = spec.and(MoviesSpecifications.hasClassification(classification));
        if (year != null && month != null) spec = spec.and(MoviesSpecifications.releasedInMonth(year, month));
        if (popular) spec = spec.and(MoviesSpecifications.isPopular());
        if (region != null) spec = spec.and(MoviesSpecifications.hasRegion(region));

        List<Movie> filtered = movieService.filterMovies(spec);
        List<MovieTranslatedDTO> translated = movieService.translateMovies(filtered, language);
        return ResponseEntity.ok(translated);
    }

    public static Specification<Movie> hasGenreTranslate(String genre, String language) {
        return (root, query, criteriaBuilder) -> {
            Join<Movie, Genre> genreJoin = root.join("genre");

            // Mapear el idioma a un atributo real
            String attributeName = switch (language.toLowerCase()) {
                case "en" -> "nameEn";
                case "fr" -> "nameFr";
                case "de" -> "nameDe";
                case "it" -> "nameIt";
                case "pt" -> "namePt";
                default -> "name"; // Español por defecto
            };

            Expression<String> genreName = genreJoin.get(attributeName);
            return criteriaBuilder.equal(genreName, genre);
        };
    }




    @GetMapping
    @Operation(summary = "Obtener películas traducidas automáticamente",
            description = """
                Devuelve películas traducidas automáticamente:
                - Si el usuario está autenticado, se usa su idioma preferido
                - Si no, se toma del header Accept-Language
                - Si no se especifica ninguno, se usa español (es) por defecto
                """)
    public ResponseEntity<List<MovieTranslatedDTO>> getTranslatedMovies(
            Authentication authentication,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        String email = authentication != null ? authentication.getName() : null;
        String language = movieService.resolveLanguage(email, acceptLanguage);
        List<Movie> allMovies = movieService.getAllMovies();
        List<MovieTranslatedDTO> translatedMovies = movieService.translateMovies(allMovies, language);
        return ResponseEntity.ok(translatedMovies);
    }

    @GetMapping("/billboard")
    @Operation(summary = "Películas en cartelera disponibles", description = "Devuelve las películas activas que tienen funciones con asientos disponibles")
    public ResponseEntity<List<Movie>> getBillboardMovies() {
        return ResponseEntity.ok(movieService.getMoviesForBillboard());
    }

}
