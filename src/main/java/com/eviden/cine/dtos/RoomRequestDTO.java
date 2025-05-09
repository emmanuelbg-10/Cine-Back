package com.eviden.cine.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para crear o actualizar una sala")
public class RoomRequestDTO {

    @NotBlank
    @Schema(description = "Nombre identificador de la sala", example = "Room Azul")
    private String nombreroom;

    @NotNull
    @Min(1)
    @Schema(description = "Cantidad de filas", example = "9")
    private Integer filas;

    @NotNull
    @Min(1)
    @Schema(description = "Cantidad de columnas", example = "12")
    private Integer columnas;

    @NotNull
    @Schema(description = "ID de la región a la que pertenece la sala", example = "1")
    private Long regionId;
}
