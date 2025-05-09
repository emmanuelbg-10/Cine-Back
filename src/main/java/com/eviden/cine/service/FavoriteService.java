package com.eviden.cine.service;

import com.eviden.cine.dtos.FavoriteDTO;
import com.eviden.cine.dtos.FavoriteTranslateDTO;
import com.eviden.cine.exception.CustomException;
import com.eviden.cine.model.Favorite;
import com.eviden.cine.model.Movie;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.FavoriteRepository;
import com.eviden.cine.repository.MovieRepository;
import com.eviden.cine.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final MovieService movieService;


    @Autowired
    public FavoriteService(FavoriteRepository favoriteRepository, UserRepository userRepository, MovieRepository movieRepository, MovieService movieService) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.movieService = movieService;
    }

    public List<FavoriteDTO> getFavoritesDTOByUserId(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserUserId(userId);
        return favorites.stream()
                .map(favorite -> new FavoriteDTO(
                        favorite.getId(),
                        favorite.getMovie(),
                        favorite.getAddedDate()
                ))
                .toList();
    }

    public List<FavoriteTranslateDTO> getFavoriteTranslated(Long userId, String language) {
        List<Favorite> favorites = favoriteRepository.findByUserUserId(userId);
        return favorites.stream()
                .map(favorite -> new FavoriteTranslateDTO(
                        favorite.getId(),
                      movieService.translateMovie(favorite.getMovie(), language),
                        favorite.getAddedDate()
                ))
                .toList();
    }

    public Favorite getFavoriteById(Long id) {
        return favoriteRepository.findById(id).orElse(null);
    }

    public List<FavoriteDTO> getFavoritesByEmail(String email) {
        List<Favorite> favorites = favoriteRepository.findByUserEmail(email);
        return favorites.stream()
                .map(favorite -> new FavoriteDTO(
                        favorite.getId(),
                        favorite.getMovie(),
                        favorite.getAddedDate()
                ))
                .toList();
    }

    public Favorite addFavorite(Long userId, int movieId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Usuario con ID " + userId + " no encontrado."));

        final Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new CustomException("Película con ID " + movieId + " no encontrada."));

        if (favoriteRepository.existsByUserUserIdAndMovieId(userId, movieId)) {
            throw new CustomException("La película con ID " + movieId + " ya está marcada como favorita por el usuario " + userId + ".");
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .movie(movie)
                .addedDate(LocalDateTime.now())
                .build();

        return favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(Long userId, int movieId) {
        if (!favoriteRepository.existsByUserUserIdAndMovieId(userId, movieId)) {
            throw new CustomException("No se encontró favorito para el usuario con ID " + userId + " y película con ID " + movieId + ".");
        }

        favoriteRepository.deleteByUserUserIdAndMovieId(userId, movieId);
    }
}
