package com.eviden.cine.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "emisiones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidad que representa una emisión de una película en una sala específica")
public class Emision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_emision")
    @Schema(description = "ID único de la emisión", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idEmision;

    @ManyToOne
    @JoinColumn(name = "id_pelicula", nullable = false)
    @Schema(description = "Película asociada a esta emisión")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "id_room", nullable = false)
    @Schema(description = "Sala donde se realiza la emisión")
    @JsonBackReference
    private Room room;

    @Column(name = "fecha_hora_inicio", nullable = false)
    @Schema(description = "Fecha y hora de inicio de la emisión", example = "2025-04-08T20:30:00")
    private LocalDateTime fechaHoraInicio;

    @Column(name = "idioma", nullable = false)
    @Schema(description = "Idioma de la emisión (audio/subtítulos)", example = "Español subtitulado")
    private String idioma;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @Schema(description = "Estado actual de la emisión", example = "ACTIVO")
    private EstadoEmision estado;

    public enum EstadoEmision {
        ACTIVO,
        CANCELADO,
        AGOTADO
    }


    @OneToMany(mappedBy = "emision", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore  // Esto evita la recursividad infinita al serializar la lista de reservas
    private List<Reservation> reservations;
}
