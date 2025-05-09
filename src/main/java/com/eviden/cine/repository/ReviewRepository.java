package com.eviden.cine.repository;

import com.eviden.cine.model.Movie;
import com.eviden.cine.model.Review;
import com.eviden.cine.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByMovie(Movie movie);
    List<Review> findByUser(User user);
    boolean existsByUserAndMovie(User user, Movie movie);
}
