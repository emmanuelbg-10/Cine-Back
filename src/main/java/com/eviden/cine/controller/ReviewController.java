package com.eviden.cine.controller;

import com.eviden.cine.dtos.ReviewRequestDTO;
import com.eviden.cine.dtos.ReviewResponseDTO;
import com.eviden.cine.model.Review;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.UserRepository;
import com.eviden.cine.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Crear una nueva reseña para una película")
    public ResponseEntity<ReviewResponseDTO> createReview(@RequestBody ReviewRequestDTO dto, Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Review created = reviewService.saveReview(dto, user);
        return ResponseEntity.ok(toResponseDTO(created));
    }

    @GetMapping("/movie/{movieId}")
    @Operation(summary = "Obtener todas las reseñas de una película")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByMovie(@PathVariable int movieId) {
        List<Review> reviews = reviewService.getReviewsByMovieId(movieId);
        return ResponseEntity.ok(
                reviews.stream().map(this::toResponseDTO).toList()
        );
    }

    @PutMapping("/{reviewId}")
    @Operation(summary = "Editar una reseña")
    public ResponseEntity<ReviewResponseDTO> updateReview(@PathVariable int reviewId,
                                                          @RequestBody ReviewRequestDTO dto,
                                                          Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Review updated = reviewService.updateReview(reviewId, dto, user);
        return ResponseEntity.ok(toResponseDTO(updated));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Eliminar una reseña")
    public ResponseEntity<String> deleteReview(@PathVariable int reviewId, Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        reviewService.deleteReview(reviewId, user);
        return ResponseEntity.ok("Reseña eliminada correctamente");
    }

    private ReviewResponseDTO toResponseDTO(Review review) {
        return new ReviewResponseDTO(
                review.getId(),
                review.getRate(),
                review.getComment(),
                review.getReviewDate(),
                review.getUser().getUsername()
        );
    }
}
