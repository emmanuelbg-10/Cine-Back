package com.eviden.cine.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "review")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@Schema(description = "Entidad que representa una reseña de una película realizada por un usuario")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único de la reseña", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @ToString.Include
    private int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "Usuario que escribió la reseña")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    @Schema(description = "Película a la que corresponde la reseña")
    @JsonBackReference
    private Movie movie;

    @Column(nullable = false)
    @Schema(description = "Valoración de la película", example = "4.5")
    private double rate;

    @Column(length = 1000)
    @Schema(description = "Comentario del usuario sobre la película", example = "Muy buena trama y excelente actuación.")
    private String comment;

    @OneToMany(mappedBy = "review", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ReviewComent> answer;

    @Column(name = "review_date", nullable = false)
    @Schema(description = "Fecha en que se realizó la reseña", example = "2025-04-22")
    private LocalDate reviewDate;
}
