package com.eviden.cine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "movie")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@Schema(description = "Entidad que representa una película en el sistema")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único de la película", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @ToString.Include
    private int id;

    @Column(nullable = false)
    @Schema(description = "Título de la película", example = "Inception")
    @ToString.Include
    private String title;

    @Column(length = 1000)
    @Schema(description = "Sinopsis de la película", example = "Un ladrón que roba secretos corporativos a través del uso de la tecnología de los sueños.")
    private String synopsis;

    // ─── Traducciones de título ──────────────────────────────
    @Column
    @Schema(description = "Título en inglés")
    private String titleEn;

    @Column
    @Schema(description = "Título en francés")
    private String titleFr;

    @Column
    @Schema(description = "Título en alemán")
    private String titleDe;

    @Column
    @Schema(description = "Título en italiano")
    private String titleIt;

    @Column
    @Schema(description = "Título en portugués")
    private String titlePt;

    // ─── Traducciones de sinopsis ─────────────────────────────
    @Column(length = 1000)
    @Schema(description = "Sinopsis en inglés")
    private String synopsisEn;

    @Column(length = 1000)
    @Schema(description = "Sinopsis en francés")
    private String synopsisFr;

    @Column(length = 1000)
    @Schema(description = "Sinopsis en alemán")
    private String synopsisDe;

    @Column(length = 1000)
    @Schema(description = "Sinopsis en italiano")
    private String synopsisIt;

    @Column(length = 1000)
    @Schema(description = "Sinopsis en portugués")
    private String synopsisPt;

    // ─── Relaciones y atributos generales ─────────────────────
    @Schema(description = "Duración en minutos", example = "148")
    private int time;

    @ManyToOne
    @JoinColumn(name = "genre_id")
    @Schema(description = "Género de la película")
    private Genre genre;

    @ManyToOne
    @JoinColumn(name = "classification_id")
    @Schema(description = "Clasificación por edad")
    private Classification classification;

    @ManyToOne
    @JoinColumn(name = "director_id")
    @Schema(description = "Nombre del director", example = "Christopher Nolan")
    private Director director;

    @ManyToMany
    @JoinTable(
            name = "movie_casting",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id")
    )
    @Schema(description = "Reparto principal", example = "Leonardo DiCaprio, Joseph Gordon-Levitt, Ellen Page")
    private List<Actor> casting;

    @Schema(description = "URL de la imagen horizontal promocional", example = "https://picsum.photos/1500/700")
    private String urlImageX;

    @Schema(description = "URL de la imagen vertical promocional", example = "https://picsum.photos/400/600")
    private String urlImageY;

    @Schema(description = "URL del tráiler de la película", example = "https://www.youtube.com/watch?v=YoHD9XEInc0")
    private String urlTrailer;

    @Schema(description = "Fecha de estreno", example = "2010-07-16")
    private LocalDate releaseDate;

    @Column(name = "is_available")
    @Schema(description = "Indica si la película está disponible actualmente", example = "true")
    private boolean isAvailable;

    @Column(name = "is_coming_soon")
    @Schema(description = "Indica si la película está marcada como 'próximamente'", example = "false")
    private boolean isComingSoon;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    private List<Favorite> favorites;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    private List<Emision> emisiones;

    @Schema(description = "Calificación de la película", example = "8.8")
    private double rating;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Review> reviews;

    public void updateAverageRating() {
        if (reviews != null && !reviews.isEmpty()) {
            double average = reviews.stream()
                    .mapToDouble(Review::getRate)
                    .average()
                    .orElse(0.0);
            this.rating = Math.round(average * 10.0) / 10.0;
        } else {
            this.rating = 0.0;
        }
    }
}
