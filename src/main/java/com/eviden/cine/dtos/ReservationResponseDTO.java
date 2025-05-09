package com.eviden.cine.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO que representa los datos de una reserva realizada, incluyendo detalles del usuario, película y QR.")
public class ReservationResponseDTO {

    @Schema(description = "Identificador único de la reserva", example = "1001")
    private Long idReserve;

    @Schema(description = "Estado de la reserva (confirmed, cancelled, pending)", example = "confirmed")
    private String status;

    @Schema(description = "Precio total pagado por la reserva", example = "21.0")
    private Double totalPrice;

    @Schema(description = "Fecha en la que se hizo la reserva", example = "2025-04-24T15:30:00")
    private String reservationDate;

    @Schema(description = "Título de la película reservada", example = "Inception")
    private String movieTitle;

    @Schema(description = "Correo electrónico del usuario que realizó la reserva", example = "usuario@cine.com")
    private String userEmail;

    @Schema(description = "Nombre del usuario que realizó la reserva", example = "Juan Pérez")
    private String userName;

    @Schema(description = "Nombre de la sala donde se proyecta la película", example = "Sala 1")
    private String roomName;

    @Schema(description = "Horario de la función", example = "2025-04-24T19:30:00")
    private String showTime;

    @Schema(description = "Lista de asientos seleccionados", example = "[\"A5\", \"A6\"]")
    private List<String> selectedSeats;

    @Schema(description = "Código QR en base64 para escanear en el cine", example = "data:image/png;base64,...")
    private String qrBase64;
}
