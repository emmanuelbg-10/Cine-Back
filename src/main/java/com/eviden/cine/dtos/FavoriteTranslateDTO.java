package com.eviden.cine.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FavoriteTranslateDTO {

    @Schema(description = "ID único del favorito", example = "1")
    private Long favoriteId;

    @Schema(description = "Película marcada como favorita")
    private MovieTranslatedDTO movie;

    @Schema(description = "Fecha en que se añadió el favorito", example = "2025-04-03T14:15:27.825093")
    private LocalDateTime addedDate;


}
