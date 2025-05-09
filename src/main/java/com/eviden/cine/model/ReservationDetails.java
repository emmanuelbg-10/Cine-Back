package com.eviden.cine.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReserveDetails;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    @JsonBackReference
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "asiento_id")
    private Asiento asiento;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
}
