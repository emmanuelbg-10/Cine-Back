package com.eviden.cine.controller;

import com.eviden.cine.dtos.ReviewComentRequestDTO;
import com.eviden.cine.dtos.ReviewComentResponseDTO;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.UserRepository;
import com.eviden.cine.service.ReviewComentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewComentControllerTest {

    @InjectMocks
    private ReviewComentController controller;

    @Mock
    private ReviewComentService service;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication auth;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addComent_success() {
        User user = new User();
        user.setUsername("usuario");

        ReviewComentRequestDTO dto = new ReviewComentRequestDTO(1L, "comentario");

        when(auth.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(service.addComent(dto, user)).thenReturn(new ReviewComentResponseDTO(1L, "comentario", "usuario", LocalDateTime.now()));

        ResponseEntity<ReviewComentResponseDTO> response = controller.addComent(dto, auth);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void getComentsByReview_success() {
        when(service.getComentsByReviewId(1L)).thenReturn(List.of());

        var response = controller.getComentsByReview(1L);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void updateComent_success() {
        User user = new User();
        user.setUsername("usuario");

        ReviewComentRequestDTO dto = new ReviewComentRequestDTO(1L, "editado");

        when(auth.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(service.updateComent(1L, dto, user)).thenReturn(new ReviewComentResponseDTO(1L, "editado", "usuario", LocalDateTime.now()));

        var response = controller.updateComent(1L, dto, auth);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void deleteComent_success() {
        User user = new User();
        when(auth.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        var response = controller.deleteComent(1L, auth);
        assertEquals("Comentario eliminado correctamente", response.getBody());
        verify(service).deleteReviewComentById(1L, user);
    }
}
