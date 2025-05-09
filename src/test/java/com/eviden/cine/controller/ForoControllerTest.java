package com.eviden.cine.controller;

import com.eviden.cine.dtos.ForoRequestDTO;
import com.eviden.cine.dtos.ForoResponseDTO;
import com.eviden.cine.model.Foro;
import com.eviden.cine.model.Movie;
import com.eviden.cine.model.User;
import com.eviden.cine.service.ForoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ForoControllerTest {

    @Mock
    private ForoService foroService;

    @InjectMocks
    private ForoController foroController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListarForos() {
        ForoResponseDTO foroResponse = ForoResponseDTO.builder()
                .id(1L)
                .title("Foro de prueba")
                .description("Descripción de prueba")
                .movieTitle("Película de prueba")
                .ownerEmail("usuario_prueba")
                .createdAt(LocalDateTime.now())
                .build();

        when(foroService.getAllForos()).thenReturn(List.of(foroResponse));

        ResponseEntity<List<ForoResponseDTO>> response = foroController.listarForos();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("Foro de prueba", response.getBody().get(0).getTitle());
        verify(foroService, times(1)).getAllForos();
    }

    @Test
    void testCrearForo() {
        ForoRequestDTO request = new ForoRequestDTO();
        request.setTitle("Nuevo Foro");
        request.setDescription("Descripción del foro");
        request.setMovieId(1);
        request.setOwnerId(1L);

        Movie movie = Movie.builder().id(1).title("Película").build();
        User owner = User.builder().userId(1L).username("usuario").build();

        Foro foro = Foro.builder()
                .id(1L)
                .title("Nuevo Foro")
                .description("Descripción del foro")
                .movie(movie)
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .build();

        ForoResponseDTO foroResponse = ForoResponseDTO.builder()
                .id(foro.getId())
                .title(foro.getTitle())
                .description(foro.getDescription())
                .movieTitle(foro.getMovie().getTitle())
                .ownerEmail(foro.getOwner().getUsername())
                .createdAt(foro.getCreatedAt())
                .build();

        when(foroService.createForo(request)).thenReturn(foro);

        ResponseEntity<ForoResponseDTO> response = foroController.crearForo(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Nuevo Foro", response.getBody().getTitle());
        verify(foroService, times(1)).createForo(request);
    }

    @Test
    void testUpdateForo() {
        ForoRequestDTO request = new ForoRequestDTO();
        request.setTitle("Foro Actualizado");
        request.setDescription("Descripción actualizada");

        Movie movie = Movie.builder().id(1).title("Película").build();
        User owner = User.builder().userId(1L).username("usuario").build();

        Foro foro = Foro.builder()
                .id(1L)
                .title("Foro Actualizado")
                .description("Descripción actualizada")
                .movie(movie)
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .build();

        ForoResponseDTO foroResponse = ForoResponseDTO.builder()
                .id(foro.getId())
                .title(foro.getTitle())
                .description(foro.getDescription())
                .movieTitle(foro.getMovie().getTitle())
                .ownerEmail(foro.getOwner().getUsername())
                .createdAt(foro.getCreatedAt())
                .build();

        when(foroService.updateForo(1L, request)).thenReturn(foro);

        ResponseEntity<ForoResponseDTO> response = foroController.updateForo(1L, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Foro Actualizado", response.getBody().getTitle());
        verify(foroService, times(1)).updateForo(1L, request);
    }

    @Test
    void testDeleteForo() {
        doNothing().when(foroService).deleteForoById(1L);

        ResponseEntity<Void> response = foroController.deleteForo(1L);

        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify(foroService, times(1)).deleteForoById(1L);
    }

    @Test
    void testGetForoById() {
        ForoResponseDTO foroResponse = ForoResponseDTO.builder()
                .id(1L)
                .title("Foro de prueba")
                .description("Descripción de prueba")
                .movieTitle("Película de prueba")
                .ownerEmail("usuario_prueba")
                .createdAt(LocalDateTime.now())
                .build();

        when(foroService.getForoById(1L)).thenReturn(foroResponse);

        ResponseEntity<ForoResponseDTO> response = foroController.getForoById(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody());
        assertEquals("Foro de prueba", response.getBody().getTitle());
        verify(foroService, times(1)).getForoById(1L);
    }
}