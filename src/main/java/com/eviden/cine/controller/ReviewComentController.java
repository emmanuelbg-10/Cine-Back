package com.eviden.cine.controller;

import com.eviden.cine.dtos.ReviewComentRequestDTO;
import com.eviden.cine.dtos.ReviewComentResponseDTO;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.UserRepository;
import com.eviden.cine.service.ReviewComentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review-coments")
@RequiredArgsConstructor
public class ReviewComentController {

    private final ReviewComentService reviewComentService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Crear una respuesta a una reseña")
    public ResponseEntity<ReviewComentResponseDTO> addComent(@RequestBody ReviewComentRequestDTO dto,
                                                             Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(reviewComentService.addComent(dto, user));
    }

    @GetMapping("/review/{reviewId}")
    @Operation(summary = "Obtener todas las respuestas de una reseña")
    public ResponseEntity<List<ReviewComentResponseDTO>> getComentsByReview(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewComentService.getComentsByReviewId(reviewId));
    }

    @PutMapping("/{comentId}")
    @Operation(summary = "Editar una respuesta a una reseña")
    public ResponseEntity<ReviewComentResponseDTO> updateComent(@PathVariable Long comentId,
                                                                @RequestBody ReviewComentRequestDTO dto,
                                                                Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(reviewComentService.updateComent(comentId, dto, user));
    }

    @DeleteMapping("/{comentId}")
    @Operation(summary = "Eliminar una respuesta a una reseña")
    public ResponseEntity<String> deleteComent(@PathVariable Long comentId,
                                               Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        reviewComentService.deleteReviewComentById(comentId, user);
        return ResponseEntity.ok("Comentario eliminado correctamente");
    }

}
