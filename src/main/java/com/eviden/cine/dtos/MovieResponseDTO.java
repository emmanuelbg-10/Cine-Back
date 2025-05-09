package com.eviden.cine.dtos;

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
public class MovieResponseDTO {
    private Long id;
    private String title;
    private String synopsis;
    private String urlImageX;
    private String urlImageY;
    private String urlTrailer;
    private GenreDTO genre;
    private ClasificationDTO classification;
    private Double rating;
    private LocalDate releaseDate;
    private Integer time;
    private String director;
    private Boolean isAvailable;
    private Boolean isComingSoon;
    private List<String> casting;
    private List<FavoriteResponseDTO> favorites;
    private List<String> review;
    private List<String> emisiones;

}
