package com.eviden.cine.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO que representa la selección de un asiento y un tipo de ticket para una reserva.")
public class AsientoTicketDTO {

    @Schema(description = "ID del asiento seleccionado por el usuario", example = "12", required = true)
    private Long asientoId;

    @Schema(description = "ID del ticket seleccionado (por ejemplo: normal, VIP, estudiante)", example = "2", required = true)
    private Long ticketId;

    @Schema(description = "Representación del asiento dentro de la sala (Ej: A5, B7)", example = "A5")
    private String asientoSala;
}
