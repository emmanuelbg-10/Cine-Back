package com.eviden.cine.service;

import com.eviden.cine.dtos.ReviewRequestDTO;
import com.eviden.cine.exception.CustomException;
import com.eviden.cine.model.Movie;
import com.eviden.cine.model.Review;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.MovieRepository;
import com.eviden.cine.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    private ReviewService reviewService;
    private ReviewRepository reviewRepository;
    private MovieRepository movieRepository;

    private User user;
    private Movie movie;
    private Review review;

    @BeforeEach
    void setUp() {
        reviewRepository = mock(ReviewRepository.class);
        movieRepository = mock(MovieRepository.class);
        reviewService = new ReviewService(reviewRepository, movieRepository);

        user = new User();
        user.setUserId(1L);

        movie = new Movie();
        movie.setId(1);
        movie.setReviews(new ArrayList<>());

        review = new Review();
        review.setId(1);
        review.setUser(user);
        review.setMovie(movie);
    }


    @Test
    void testSaveReviewSuccess() {
        ReviewRequestDTO dto = new ReviewRequestDTO(1, 4.5, "Muy buena");

        when(movieRepository.findById(1)).thenReturn(Optional.of(movie));
        when(reviewRepository.existsByUserAndMovie(user, movie)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);

        Review result = reviewService.saveReview(dto, user);

        assertEquals(user, result.getUser());
        assertEquals(movie, result.getMovie());
    }

    @Test
    void testSaveReviewAlreadyExists() {
        ReviewRequestDTO dto = new ReviewRequestDTO(1, 4.5, "Ya comenté");

        when(movieRepository.findById(1)).thenReturn(Optional.of(movie));
        when(reviewRepository.existsByUserAndMovie(user, movie)).thenReturn(true);

        assertThrows(CustomException.class, () -> reviewService.saveReview(dto, user));
    }

    @Test
    void testUpdateReviewSuccess() {
        ReviewRequestDTO dto = new ReviewRequestDTO(1, 5.0, "Actualizada");
        Review existing = new Review();
        existing.setUser(user);
        existing.setMovie(movie);
        existing.setId(1);

        when(reviewRepository.findById(1)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(Review.class))).thenReturn(existing);
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);

        Review result = reviewService.updateReview(1, dto, user);
        assertEquals("Actualizada", result.getComment());
        assertEquals(5.0, result.getRate());
    }

    @Test
    void testUpdateReviewWrongUser() {
        ReviewRequestDTO dto = new ReviewRequestDTO(1, 2.0, "Otra");
        User otro = new User();
        otro.setUserId(999L);
        review.setUser(user);

        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));
        assertThrows(CustomException.class, () -> reviewService.updateReview(1, dto, otro));
    }

    @Test
    void testDeleteReviewSuccess() {
        review.setMovie(movie);
        review.setUser(user);
        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));
        when(reviewRepository.findByMovie(movie)).thenReturn(List.of());
        doNothing().when(reviewRepository).delete(review);

        assertDoesNotThrow(() -> reviewService.deleteReview(1, user));
    }

    @Test
    void testDeleteReviewNotOwner() {
        User otro = new User();
        otro.setUserId(42L);
        review.setUser(user);

        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));
        assertThrows(CustomException.class, () -> reviewService.deleteReview(1, otro));
    }
}
