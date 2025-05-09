package com.eviden.cine.dtos;

import com.eviden.cine.model.Movie;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO que representa un favorito simplificado, utilizado para respuestas al cliente")
public class FavoriteDTO {

    @Schema(description = "ID único del favorito", example = "1")
    private Long favoriteId;

    @Schema(description = "Película marcada como favorita")
    private Movie movie;

    @Schema(description = "Fecha en que se añadió el favorito", example = "2025-04-03T14:15:27.825093")
    private LocalDateTime addedDate;
}
