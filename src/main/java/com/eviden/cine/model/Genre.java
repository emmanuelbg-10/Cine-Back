package com.eviden.cine.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "genre")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String name; // Español

    private String nameEn;
    private String nameFr;
    private String nameDe;
    private String nameIt;
    private String namePt;

    public String getTranslatedName(String lang) {
        return switch (lang.toLowerCase()) {
            case "en" -> nameEn != null ? nameEn : name;
            case "fr" -> nameFr != null ? nameFr : name;
            case "de" -> nameDe != null ? nameDe : name;
            case "it" -> nameIt != null ? nameIt : name;
            case "pt" -> namePt != null ? namePt : name;
            default -> name;
        };
    }
}
