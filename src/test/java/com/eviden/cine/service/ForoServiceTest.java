package com.eviden.cine.service;

import com.eviden.cine.dtos.ForoRequestDTO;
import com.eviden.cine.dtos.ForoResponseDTO;
import com.eviden.cine.model.Foro;
import com.eviden.cine.model.Movie;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.ForoRepository;
import com.eviden.cine.repository.MovieRepository;
import com.eviden.cine.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ForoServiceTest {

    @Mock
    private ForoRepository foroRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ForoService foroService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllForos() {
        Foro foro = Foro.builder()
                .id(1L)
                .title("Foro de prueba")
                .description("Descripción de prueba")
                .movie(Movie.builder().title("Película de prueba").build())
                .owner(User.builder().username("usuario_prueba").build())
                .createdAt(LocalDateTime.now())
                .build();

        when(foroRepository.findAll()).thenReturn(List.of(foro));

        List<ForoResponseDTO> foros = foroService.getAllForos();

        assertEquals(1, foros.size());
        assertEquals("Foro de prueba", foros.get(0).getTitle());
        verify(foroRepository, times(1)).findAll();
    }

    @Test
    void testCreateForo() {
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

        when(movieRepository.findById(1)).thenReturn(Optional.of(movie));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(foroRepository.save(any(Foro.class))).thenReturn(foro);

        Foro result = foroService.createForo(request);

        assertNotNull(result);
        assertEquals("Nuevo Foro", result.getTitle());
        verify(movieRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(1L);
        verify(foroRepository, times(1)).save(any(Foro.class));
    }

    @Test
    void testUpdateForo() {
        ForoRequestDTO request = new ForoRequestDTO();
        request.setTitle("Foro Actualizado");
        request.setDescription("Descripción actualizada");

        Foro foro = Foro.builder()
                .id(1L)
                .title("Foro Original")
                .description("Descripción original")
                .build();

        when(foroRepository.findById(1L)).thenReturn(Optional.of(foro));
        when(foroRepository.save(any(Foro.class))).thenReturn(foro);

        Foro result = foroService.updateForo(1L, request);

        assertNotNull(result);
        assertEquals("Foro Actualizado", result.getTitle());
        assertEquals("Descripción actualizada", result.getDescription());
        verify(foroRepository, times(1)).findById(1L);
        verify(foroRepository, times(1)).save(any(Foro.class));
    }

    @Test
    void testDeleteForoById() {
        doNothing().when(foroRepository).deleteById(1L);

        foroService.deleteForoById(1L);

        verify(foroRepository, times(1)).deleteById(1L);
    }

    @Test
    void testCreateForoThrowsEntityNotFoundException() {
        ForoRequestDTO request = new ForoRequestDTO();
        request.setMovieId(1);
        request.setOwnerId(1L);

        when(movieRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> foroService.createForo(request));
        verify(movieRepository, times(1)).findById(1);
    }

    @Test
    void testGetForoById() {
        Foro foro = Foro.builder()
                .id(1L)
                .title("Foro de prueba")
                .description("Descripción de prueba")
                .movie(Movie.builder().title("Película de prueba").build())
                .owner(User.builder().username("usuario_prueba").build())
                .createdAt(LocalDateTime.now())
                .build();

        when(foroRepository.findById(1L)).thenReturn(Optional.ofNullable(foro));

        ForoResponseDTO result = foroService.getForoById(1L);

        assertEquals(1, result.getId() );
        assertEquals("Foro de prueba", result.getTitle());
        verify(foroRepository, times(1)).findById(1L);
    }
}