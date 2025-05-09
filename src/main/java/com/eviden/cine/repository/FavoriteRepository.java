package com.eviden.cine.repository;

import com.eviden.cine.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserUserId(Long userId);
    List<Favorite> findByUserEmail(String email);
    boolean existsByUserUserIdAndMovieId(Long userId, int movieId);
    void deleteByUserUserIdAndMovieId(Long userId, int movieId);
}
