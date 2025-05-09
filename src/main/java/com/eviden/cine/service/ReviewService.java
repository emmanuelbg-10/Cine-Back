package com.eviden.cine.service;

import com.eviden.cine.dtos.ReviewRequestDTO;
import com.eviden.cine.exception.CustomException;
import com.eviden.cine.model.Movie;
import com.eviden.cine.model.Review;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.MovieRepository;
import com.eviden.cine.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;

    @Transactional
    public Review saveReview(ReviewRequestDTO dto, User user) {
        Movie movie = movieRepository.findById(dto.movieId())
                .orElseThrow(() -> new EntityNotFoundException("Película no encontrada"));

        if (reviewRepository.existsByUserAndMovie(user, movie)) {
            throw new CustomException("El usuario ya ha realizado una reseña para esta película");
        }

        Review review = Review.builder()
                .movie(movie)
                .user(user)
                .rate(dto.rate())
                .comment(dto.comment())
                .reviewDate(LocalDate.now())
                .build();

        Review saved = reviewRepository.save(review);

        movie.getReviews().add(saved);
        movie.updateAverageRating();
        movieRepository.save(movie);

        return saved;
    }

    public List<Review> getReviewsByMovieId(int movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("Película no encontrada"));
        return reviewRepository.findByMovie(movie);
    }

    public List<Review> getReviewsByUser(User user) {
        return reviewRepository.findByUser(user);
    }

    @Transactional
    public Review updateReview(int reviewId, ReviewRequestDTO dto, User currentUser) {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Reseña no encontrada"));

        if (!existingReview.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new CustomException("No tienes permisos para modificar esta reseña");
        }

        existingReview.setRate(dto.rate());
        existingReview.setComment(dto.comment());
        existingReview.setReviewDate(LocalDate.now());

        Review saved = reviewRepository.save(existingReview);

        Movie movie = existingReview.getMovie();
        movie.updateAverageRating();
        movieRepository.save(movie);

        return saved;
    }

    @Transactional
    public void deleteReview(int reviewId, User currentUser) {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Reseña no encontrada"));

        if (!existingReview.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new CustomException("No tienes permisos para eliminar esta reseña");
        }

        Movie movie = existingReview.getMovie();

        reviewRepository.delete(existingReview);

        List<Review> reviewsRestantes = reviewRepository.findByMovie(movie);
        movie.setRating(0.0);
        if (!reviewsRestantes.isEmpty()) {
            double promedio = reviewsRestantes.stream()
                    .mapToDouble(Review::getRate)
                    .average()
                    .orElse(0.0);
            movie.setRating(Math.round(promedio * 10.0) / 10.0);
        }

        movieRepository.save(movie);
    }
}
