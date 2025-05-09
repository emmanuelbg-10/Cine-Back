package com.eviden.cine.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorites")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidad que representa una relación de favorito entre un usuario y una película")
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    @Schema(description = "ID único del favorito", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference(value = "user-favorites")
    @Schema(description = "Usuario al que pertenece este favorito")
    private User user;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonBackReference(value = "movie-favorites")
    @Schema(description = "Película marcada como favorita por el usuario")
    private Movie movie;

    @Column(name = "added_date")
    @Schema(description = "Fecha en la que se añadió el favorito", example = "2025-04-03T14:15:27.825093")
    private LocalDateTime addedDate;
}
