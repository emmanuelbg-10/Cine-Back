package com.eviden.cine.dtos;

import com.eviden.cine.model.Actor;
import com.eviden.cine.model.Classification;
import com.eviden.cine.model.Director;
import com.eviden.cine.model.Genre;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO que representa los datos de una película utilizados para crear o actualizar")
public class MovieDTO {

    @Schema(description = "ID único de la película", example = "1")
    private int id;

    @Schema(description = "Título de la película", example = "Titanic")
    private String title;

    @Schema(description = "Sinopsis de la película", example = "Una historia de amor durante el trágico hundimiento del Titanic")
    private String synopsis;

    @Schema(description = "Duración de la película en minutos", example = "195")
    private int time;

    @Schema(description = "Nombre del género al que pertenece la película", example = "Romance")
    private Genre genre;

    @Schema(description = "Clasificación por edad de la película", example = "PG13")
    private Classification classification;

    @Schema(description = "Director de la película", example = "James Cameron")
    private Director director;

    @Schema(description = "Reparto de la película", example = "Leonardo DiCaprio, Kate Winslet")
    private List<Actor> casting;

    @Schema(description = "URL de la imagen horizontal promocional", example = "https://example.com/banner.jpg")
    private String urlImageX;

    @Schema(description = "URL de la imagen vertical promocional", example = "https://example.com/poster.jpg")
    private String urlImageY;

    @Schema(description = "URL del tráiler de la película", example = "https://youtube.com/watch?v=xyz123")
    private String urlTrailer;

    @Schema(description = "Fecha de estreno de la película", example = "2025-04-03")
    private LocalDate releaseDate;

    @Schema(description = "Indica si la película esta disponible actualmente", example = "true")
    private Boolean isAvailable;

    @Schema(description = "Indica si la pelicula esta marcada como 'próximamente'", example = "false")
    private Boolean isComingSoon;

    @Schema(description = "Calificación de la película", example = "8.8")
    private Double rating;




}
