package com.eviden.cine.dtos;

import com.eviden.cine.model.Emision.EstadoEmision;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para crear o actualizar una emisión")
public class EmisionDTO {

    @NotNull
    @Schema(description = "ID de la película", example = "1")
    private Integer idPelicula;

    @Schema(description = "ID de la room (opcional, si no se proporciona se asignará automáticamente)", example = "2")
    private Long idRoom;

    @NotNull
    @Future
    @Schema(description = "Fecha y hora de inicio de la emisión", example = "2025-04-08T20:30:00")
    private LocalDateTime fechaHoraInicio;

    @NotBlank
    @Schema(description = "Idioma de la emisión (audio/subtítulos)", example = "Español subtitulado")
    private String idioma;

    @NotNull
    @Schema(description = "Estado actual de la emisión", example = "ACTIVO")
    private EstadoEmision estado;
}
