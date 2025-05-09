package com.eviden.cine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "actor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Actor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @ManyToMany(mappedBy = "casting")
    @JsonIgnore
    private List<Movie> movies;
}

