package com.eviden.cine.dtos;

import com.eviden.cine.model.Emision.EstadoEmision;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "DTO de respuesta para las emisiones creadas")
public class EmisionResponseDTO {

    @Schema(description = "ID único de la emisión", example = "10")
    private Long idEmision;

    @Schema(description = "Título de la película", example = "Inception")
    private String movieTitle;

    @Schema(description = "ID de la sala", example = "5")
    private Long roomId;

    @Schema(description = "Nombre de la sala", example = "Sala Azul")
    private String roomName;

    @Schema(description = "Capacidad total de la sala", example = "120")
    private Integer roomCapacity;

    @Schema(description = "Fecha y hora de inicio de la emisión", example = "2025-06-15T20:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaHoraInicio;


    @Schema(description = "Idioma de la emisión", example = "Español subtitulado")
    private String idioma;

    @Schema(description = "Estado actual de la emisión", example = "ACTIVO")
    private EstadoEmision estado;
}
