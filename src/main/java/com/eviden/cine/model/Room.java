package com.eviden.cine.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@Schema(description = "Entidad que representa una room dentro del cine")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_room")
    @Schema(description = "ID único de la room", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @ToString.Include
    private Long idroom;

    @Column(name = "nombre_room", nullable = false)
    @Schema(description = "Nombre identificador de la room", example = "Room A")
    @ToString.Include
    private String nombreroom;

    @Column(name = "capacidad")
    @Schema(description = "Capacidad total de asientos de la room", example = "120")
    @ToString.Include
    private Integer capacidad;

    @Column(name = "filas")
    @Schema(description = "Número de filas de asientos en la room", example = "9")
    private int filas;

    @Column(name = "columnas")
    @Schema(description = "Número de columnas de asientos en la room", example = "12")
    private int columnas;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Schema(description = "Lista de asientos asociados a la room")
    @ToString.Exclude
    @Builder.Default
    private List<Asiento> asientos = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JsonManagedReference
    @Schema(description = "Lista de emisiones asociadas a la sala")
    @ToString.Exclude
    @Builder.Default
    private List<Emision> emisiones = new ArrayList<>();



    @ManyToOne
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    public void agregarAsiento(Asiento asiento) {
        asientos.add(asiento);
        asiento.setRoom(this);
    }

    public void quitarAsiento(Asiento asiento) {
        asientos.remove(asiento);
        asiento.setRoom(null);
    }
}
