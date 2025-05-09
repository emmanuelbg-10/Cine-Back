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
@Schema(description = "DTO utilizado para enviar los datos necesarios para crear una reserva.")
public class ReservationRequestDTO {

    @Schema(description = "ID del usuario que realiza la reserva", example = "1", required = true)
    private Long userId;

    @Schema(description = "ID de la emisión (película + horario) seleccionada", example = "5", required = true)
    private Long emisionId;

    @Schema(description = "Lista de objetos con información de los asientos y tickets seleccionados", required = true)
    private List<AsientoTicketDTO> asientos;

    @Schema(description = "Total en céntimos del importe de la reserva (se dividirá por 100 en el backend)", example = "2100", required = true)
    private Double total;
}
