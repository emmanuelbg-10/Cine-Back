package com.eviden.cine.dtos;

import com.eviden.cine.model.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieTranslatedDTO {
    private int id;
    private String title;
    private String synopsis;
    private String urlImageX;
    private String urlImageY;
    private String urlTrailer;
    private GenreTranslatedDTO genre;
    private Classification classification;
    private Double rating;
    private LocalDate releaseDate;
    private Integer time;
    private Director director;
    private Boolean isAvailable;
    private Boolean isComingSoon;


    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> review;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorite> favorites;

    @JsonIgnore
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Emision> emisiones;

    @ManyToMany
    @JoinTable(
            name = "movie_actor",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id")
    )
    private List<Actor> casting;


}
