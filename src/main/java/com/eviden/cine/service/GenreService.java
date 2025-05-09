package com.eviden.cine.service;

import com.eviden.cine.dtos.GenreTranslatedDTO;
import com.eviden.cine.model.Genre;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.GenreRepository;
import com.eviden.cine.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenreService {

    private final GenreRepository genreRepository;
    private final UserRepository userRepository;

    public GenreService(GenreRepository genreRepository, UserRepository userRepository) {
        this.genreRepository = genreRepository;
        this.userRepository = userRepository;
    }

    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    public Genre createGenre(Genre genre) {
        return genreRepository.save(genre);
    }

    public Genre getGenreById(int id) {
        return genreRepository.findById(id).orElse(null);
    }

    public void deleteGenreById(int id) {
        genreRepository.deleteById(id);
    }

    public List<GenreTranslatedDTO> getGenresForUserLanguage(String email) {
        String language = userRepository.findByEmail(email)
                .map(User::getPreferredLanguage)
                .orElse("es");

        return genreRepository.findAll().stream()
                .map(g -> {
                    String name = switch (language.toLowerCase()) {
                        case "en" -> g.getNameEn();
                        default -> g.getName(); // español por defecto
                    };
                    return new GenreTranslatedDTO(g.getId(), name);
                })
                .toList();
    }

    public List<GenreTranslatedDTO> getGenresByUserOrHeader(String email, String acceptLang) {
        String lang = "es";

        if (email != null) {
            lang = userRepository.findByEmail(email)
                    .map(User::getPreferredLanguage)
                    .orElse("es");
        } else if (acceptLang != null && !acceptLang.isBlank()) {
            lang = acceptLang;
        }

        String finalLang = lang.toLowerCase();

        return genreRepository.findAll().stream()
                .map(g -> {
                    String name = switch (finalLang) {
                        case "en" -> g.getNameEn();
                        case "fr" -> g.getNameFr();
                        case "de" -> g.getNameDe();
                        case "it" -> g.getNameIt();
                        case "pt" -> g.getNamePt();
                        default   -> g.getName(); // español
                    };
                    return new GenreTranslatedDTO(g.getId(), name);
                })
                .toList();
    }




}
