package com.eviden.cine.controller;

import com.eviden.cine.dtos.ReviewRequestDTO;
import com.eviden.cine.dtos.ReviewResponseDTO;
import com.eviden.cine.model.Review;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.UserRepository;
import com.eviden.cine.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ReviewControllerTest {

    private ReviewController controller;
    private ReviewService reviewService;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        reviewService = mock(ReviewService.class);
        userRepository = mock(UserRepository.class);
        controller = new ReviewController(reviewService, userRepository);
    }

    @Test
    void testCreateReview() {
        ReviewRequestDTO requestDTO = new ReviewRequestDTO(3, 5,"increible");
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");

        User user = new User();
        user.setUsername("Usuario Prueba");

        Review review = new Review();
        review.setId(1);
        review.setRate(5);
        review.setComment("Muy buena película");
        review.setReviewDate(LocalDate.now());
        review.setUser(user);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(reviewService.saveReview(requestDTO, user)).thenReturn(review);

        ResponseEntity<ReviewResponseDTO> response = controller.createReview(requestDTO, auth);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(review.getId(), response.getBody().id());
        assertEquals(review.getComment(), response.getBody().comment());
    }

    @Test
    void testGetReviewsByMovie() {
        int movieId = 10;

        Review review = new Review();
        review.setId(1);
        review.setRate(4);
        review.setComment("Buena");
        review.setReviewDate(LocalDate.now());
        User user = new User();
        user.setUsername("Usuario");
        review.setUser(user);

        when(reviewService.getReviewsByMovieId(movieId)).thenReturn(List.of(review));

        ResponseEntity<List<ReviewResponseDTO>> response = controller.getReviewsByMovie(movieId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("Buena", response.getBody().get(0).comment());
    }

    @Test
    void testUpdateReview() {
        int reviewId = 1;
        ReviewRequestDTO dto = new ReviewRequestDTO(3, 5,"Actualizado");
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");

        User user = new User();
        user.setUsername("Usuario");

        Review updatedReview = new Review();
        updatedReview.setId(reviewId);
        updatedReview.setRate(3);
        updatedReview.setComment("Actualizado");
        updatedReview.setReviewDate(LocalDate.now());
        updatedReview.setUser(user);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(reviewService.updateReview(reviewId, dto, user)).thenReturn(updatedReview);

        ResponseEntity<ReviewResponseDTO> response = controller.updateReview(reviewId, dto, auth);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Actualizado", response.getBody().comment());
    }

    @Test
    void testDeleteReview() {
        int reviewId = 1;
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");

        User user = new User();
        user.setUsername("Usuario");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        doNothing().when(reviewService).deleteReview(reviewId, user);

        ResponseEntity<String> response = controller.deleteReview(reviewId, auth);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Reseña eliminada correctamente", response.getBody());
    }
}
