package com.eviden.cine.dtos;

import com.eviden.cine.model.Emision;
import com.eviden.cine.model.Emision.EstadoEmision;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "DTO sin traducción, para usos internos o endpoints simples")
public record EmisionFrontDTO(

        @Schema(description = "ID de la emisión", example = "15")
        Long id,

        @Schema(description = "Título de la película (sin traducir)", example = "Dune: Part Two")
        String pelicula,

        @Schema(description = "ID de la sala", example = "5")
        Long roomId,

        @Schema(description = "Nombre de la sala", example = "Room A")
        String sala,

        @Schema(description = "ID de la región", example = "2")
        Long regionId,

        @Schema(description = "Nombre de la región", example = "La Orotava")
        String regionName,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @Schema(description = "Fecha y hora de inicio", example = "2025-05-06T20:30:00")
        LocalDateTime inicio,

        @Schema(description = "Idioma de la emisión", example = "VOSE")
        String idioma,

        @Schema(description = "Estado de la emisión", example = "ACTIVO")
        EstadoEmision estado

) {

    public static EmisionFrontDTO of(Emision e) {
        return new EmisionFrontDTO(
                e.getIdEmision(),
                e.getMovie().getTitle(),
                e.getRoom().getIdroom(),
                e.getRoom().getNombreroom(),
                e.getRoom().getRegion() != null ? e.getRoom().getRegion().getId() : null,
                e.getRoom().getRegion() != null ? e.getRoom().getRegion().getName() : null,
                e.getFechaHoraInicio(),
                e.getIdioma(),
                e.getEstado()
        );
    }
}
