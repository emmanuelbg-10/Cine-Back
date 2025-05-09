package com.eviden.cine.service;

import com.eviden.cine.dtos.ReviewComentRequestDTO;
import com.eviden.cine.dtos.ReviewComentResponseDTO;
import com.eviden.cine.model.Review;
import com.eviden.cine.model.ReviewComent;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.ReviewComentRepository;
import com.eviden.cine.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewComentServiceTest {

    @InjectMocks
    private ReviewComentService service;

    @Mock
    private ReviewComentRepository reviewComentRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addComent_success() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("usuario");

        Review review = new Review();
        review.setId(1);

        ReviewComentRequestDTO dto = new ReviewComentRequestDTO(1L, "Comentario");

        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));
        when(reviewComentRepository.save(any())).thenAnswer(i -> {
            ReviewComent r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        ReviewComentResponseDTO response = service.addComent(dto, user);

        assertEquals("Comentario", response.text());
        assertEquals("usuario", response.authorUsername());
        assertNotNull(response.createdAt());
        verify(reviewComentRepository).save(any());
    }

    @Test
    void addComent_reviewNotFound() {
        ReviewComentRequestDTO dto = new ReviewComentRequestDTO(99L, "test");
        when(reviewRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.addComent(dto, new User()));
    }

    @Test
    void getComentsByReviewId_success() {
        Review review = new Review();
        review.setId(1);

        User user = new User();
        user.setUsername("usuario");

        ReviewComent coment = ReviewComent.builder()
                .id(1L).review(review).author(user).text("texto").createdAt(LocalDateTime.now())
                .build();

        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));
        when(reviewComentRepository.findByReview(review)).thenReturn(List.of(coment));

        var result = service.getComentsByReviewId(1L);
        assertEquals(1, result.size());
    }

    @Test
    void getComentsByReviewId_notFound() {
        when(reviewRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.getComentsByReviewId(1L));
    }

    @Test
    void updateComent_success() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("usuario");

        ReviewComentRequestDTO dto = new ReviewComentRequestDTO(1L, "Nuevo texto");

        ReviewComent coment = ReviewComent.builder()
                .id(1L).text("Viejo texto").author(user).createdAt(LocalDateTime.now())
                .build();

        when(reviewComentRepository.findById(1L)).thenReturn(Optional.of(coment));
        when(reviewComentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var response = service.updateComent(1L, dto, user);

        assertEquals("Nuevo texto", response.text());
    }

    @Test
    void updateComent_forbidden() {
        User user = new User();
        user.setUserId(1L);

        User otroUsuario = new User();
        otroUsuario.setUserId(2L);

        ReviewComent coment = ReviewComent.builder().id(1L).author(otroUsuario).build();

        when(reviewComentRepository.findById(1L)).thenReturn(Optional.of(coment));

        assertThrows(SecurityException.class, () -> service.updateComent(1L, new ReviewComentRequestDTO(1L, "edit"), user));
    }

    @Test
    void deleteComent_success() {
        User user = new User();
        user.setUserId(1L);

        ReviewComent coment = ReviewComent.builder().id(1L).author(user).build();

        when(reviewComentRepository.findById(1L)).thenReturn(Optional.of(coment));

        service.deleteReviewComentById(1L, user);

        verify(reviewComentRepository).delete(coment);
    }

    @Test
    void deleteComent_forbidden() {
        User user = new User();
        user.setUserId(1L);

        User otro = new User();
        otro.setUserId(2L);

        ReviewComent coment = ReviewComent.builder().id(1L).author(otro).build();

        when(reviewComentRepository.findById(1L)).thenReturn(Optional.of(coment));

        assertThrows(SecurityException.class, () -> service.deleteReviewComentById(1L, user));
    }

    @Test
    void getAllReviewComents_returnsAll() {
        when(reviewComentRepository.findAll()).thenReturn(Collections.emptyList());
        var result = service.getAllReviewComents();
        assertTrue(result.isEmpty());
    }
}
