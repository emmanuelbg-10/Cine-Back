package com.eviden.cine.service;

import com.eviden.cine.dtos.ReviewComentRequestDTO;
import com.eviden.cine.dtos.ReviewComentResponseDTO;
import com.eviden.cine.model.Review;
import com.eviden.cine.model.ReviewComent;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.ReviewComentRepository;
import com.eviden.cine.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewComentService {

    private final ReviewComentRepository reviewComentRepository;
    private final ReviewRepository reviewRepository;

    public ReviewComentResponseDTO addComent(ReviewComentRequestDTO dto, User author) {
        Review review = reviewRepository.findById(Math.toIntExact(dto.reviewId()))
                .orElseThrow(() -> new EntityNotFoundException("Reseña no encontrada"));

        ReviewComent coment = ReviewComent.builder()
                .review(review)
                .author(author)
                .text(dto.text())
                .createdAt(LocalDateTime.now())
                .build();

        ReviewComent saved = reviewComentRepository.save(coment);

        return new ReviewComentResponseDTO(
                saved.getId(),
                saved.getText(),
                author.getUsername(),
                author.getEmail(),
                saved.getCreatedAt()
        );
    }

    public List<ReviewComentResponseDTO> getComentsByReviewId(Long reviewId) {
        Review review = reviewRepository.findById(reviewId.intValue())
                .orElseThrow(() -> new EntityNotFoundException("Reseña no encontrada"));

        return reviewComentRepository.findByReview(review).stream()
                .map(c -> new ReviewComentResponseDTO(
                        c.getId(),
                        c.getText(),
                        c.getAuthor().getUsername(),
                        c.getAuthor().getEmail(),
                        c.getCreatedAt()
                )).toList();
    }

    public ReviewComentResponseDTO updateComent(Long comentId, ReviewComentRequestDTO dto, User currentUser) {
        ReviewComent coment = reviewComentRepository.findById(comentId)
                .orElseThrow(() -> new EntityNotFoundException("Comentario no encontrado"));

        if (!coment.getAuthor().getUserId().equals(currentUser.getUserId())) {
            throw new SecurityException("No tienes permisos para editar este comentario");
        }

        coment.setText(dto.text());
        coment.setCreatedAt(LocalDateTime.now());

        ReviewComent updated = reviewComentRepository.save(coment);

        return new ReviewComentResponseDTO(
                updated.getId(),
                updated.getText(),
                updated.getAuthor().getUsername(),
                updated.getAuthor().getEmail(),
                updated.getCreatedAt()
        );
    }

    public List<ReviewComent> getAllReviewComents() {
        return reviewComentRepository.findAll();
    }

    public void deleteReviewComentById(Long comentId, User currentUser) {
        ReviewComent coment = reviewComentRepository.findById(comentId)
                .orElseThrow(() -> new EntityNotFoundException("Comentario no encontrado"));

        if (!coment.getAuthor().getUserId().equals(currentUser.getUserId())) {
            throw new SecurityException("No tienes permisos para eliminar este comentario");
        }

        reviewComentRepository.delete(coment);
    }
}
