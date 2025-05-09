package com.eviden.cine.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "asientos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@Schema(description = "Entidad que representa un asiento dentro de una room de cine")
public class Asiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asiento")
    @Schema(description = "ID único del asiento", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @ToString.Include
    private Long idAsiento;

    @Column(nullable = false)
    @Schema(description = "Letra de la fila del asiento", example = "B")
    @ToString.Include
    private String fila;

    @Column(nullable = false)
    @Schema(description = "Número de la columna del asiento", example = "7")
    @ToString.Include
    private Integer columna;

    @Column(name = "tipo_asiento", nullable = false)
    @Schema(description = "Tipo de asiento (normal, VIP, minusvalido)", example = "VIP")
    @ToString.Include
    private String tipoAsiento;

    @Column(nullable = false)
    @Schema(description = "Indica si el asiento está disponible", example = "true")
    @ToString.Include
    private boolean disponible;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_room")
    @JsonBackReference
    @ToString.Exclude
    @Schema(description = "Room a la que pertenece este asiento")
    private Room room;
}
