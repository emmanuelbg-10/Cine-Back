package com.eviden.cine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Schema(description = "Entidad que representa una reserva de entradas para una emisión específica por parte de un usuario.")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único de la reserva", example = "1001")
    private Long idReserve;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    @Schema(description = "Usuario que realiza la reserva", hidden = true)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "emision_id", nullable = false)
    @Schema(description = "Emisión de película y horario asociada a la reserva")
    private Emision emision;

    @Schema(description = "Precio total de la reserva", example = "21.0")
    private Double totalPrice;

    @Schema(description = "Fecha y hora en que se realizó la reserva", example = "2025-04-24T15:30:00")
    private LocalDateTime reservationDate;

    @Schema(description = "Estado de la reserva", example = "confirmed")
    private String status;

    @Lob
    @Column(name = "qr_content")
    @Basic(fetch = FetchType.LAZY)
    @Schema(description = "Contenido del código QR en binario", accessMode = Schema.AccessMode.READ_ONLY)
    private byte[] qrContent;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Schema(description = "Lista de detalles de la reserva (asientos y tickets seleccionados)")
    private List<ReservationDetails> reserveDetails;
}
