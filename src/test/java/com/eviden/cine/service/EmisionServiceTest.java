package com.eviden.cine.service;

import com.eviden.cine.dtos.EmisionDTO;
import com.eviden.cine.dtos.EmisionFrontDTO;
import com.eviden.cine.dtos.EmisionResponseDTO;
import com.eviden.cine.exception.CustomException;
import com.eviden.cine.model.Emision;
import com.eviden.cine.model.Movie;
import com.eviden.cine.model.Room;
import com.eviden.cine.repository.EmisionRepository;
import com.eviden.cine.repository.MovieRepository;
import com.eviden.cine.repository.RoomRepository;
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

class EmisionServiceTest {

    @Mock
    private EmisionRepository emisionRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomAssignmentService roomAssignmentService;

    @InjectMocks
    private EmisionService emisionService;

    private EmisionDTO dto;
    private Movie movie;
    private Room room;
    private Emision emision;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        movie = Movie.builder()
                .id(1)
                .title("Inception")
                .build();

        room = Room.builder()
                .idroom(1L)
                .nombreroom("Room A")
                .capacidad(100)
                .build();

        dto = EmisionDTO.builder()
                .idPelicula(1)
                .idRoom(1L)
                .fechaHoraInicio(LocalDateTime.now().plusHours(1))
                .idioma("Español")
                .estado(Emision.EstadoEmision.ACTIVO)
                .build();

        emision = Emision.builder()
                .idEmision(1L)
                .movie(movie)
                .room(room)
                .fechaHoraInicio(dto.getFechaHoraInicio())
                .idioma(dto.getIdioma())
                .estado(dto.getEstado())
                .build();
    }

    @Test
    void testObtenerTodas() {
        when(emisionRepository.findAll()).thenReturn(List.of(emision));
        List<Emision> result = emisionService.obtenerTodas();
        assertEquals(1, result.size());
        verify(emisionRepository).findAll();
    }

    @Test
    void testObtenerPorIdFound() {
        when(emisionRepository.findById(1L)).thenReturn(Optional.of(emision));
        Optional<Emision> result = emisionService.obtenerPorId(1L);
        assertTrue(result.isPresent());
        assertEquals(emision, result.get());
    }

    @Test
    void testObtenerPorIdNotFound() {
        when(emisionRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<Emision> result = emisionService.obtenerPorId(99L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGuardarDesdeDTOOk() {
        when(movieRepository.findById(1)).thenReturn(Optional.of(movie));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        // Suponiendo que se espera true cuando se verifica la disponibilidad de la sala:
        when(roomAssignmentService.estaSalaDisponible(any(Room.class), any(LocalDateTime.class), anyInt())).thenReturn(true);
        when(emisionRepository.save(any(Emision.class))).thenReturn(emision);

        Emision saved = emisionService.guardarDesdeDTO(dto);

        assertNotNull(saved);
        assertEquals(movie, saved.getMovie());
        assertEquals(room, saved.getRoom());
        assertEquals("Español", saved.getIdioma());
    }

    @Test
    void testGuardarDesdeDTOMovieNotFound() {
        when(movieRepository.findById(1)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class, () -> emisionService.guardarDesdeDTO(dto));
        assertEquals("Película no encontrada con ID: 1", ex.getMessage());
    }

    @Test
    void testGuardarDesdeDTORoomNotFound() {
        when(movieRepository.findById(1)).thenReturn(Optional.of(movie));
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class, () -> emisionService.guardarDesdeDTO(dto));
        assertEquals("Sala no encontrada con ID: 1", ex.getMessage());
    }

    @Test
    void testActualizarDesdeDTOOk() {
        when(emisionRepository.findById(1L)).thenReturn(Optional.of(emision));
        when(movieRepository.findById(1)).thenReturn(Optional.of(movie));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomAssignmentService.estaSalaDisponible(any(), any(), anyInt())).thenReturn(true);
        when(emisionRepository.save(any(Emision.class))).thenReturn(emision);

        Emision result = emisionService.actualizarDesdeDTO(1L, dto);

        assertEquals(movie, result.getMovie());
        assertEquals(room, result.getRoom());
        assertEquals("Español", result.getIdioma());
    }


    @Test
    void testActualizarDesdeDTOEmisionNotFound() {
        when(emisionRepository.findById(2L)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> emisionService.actualizarDesdeDTO(2L, dto));

        assertEquals("Emisión no encontrada con ID: 2", ex.getMessage());
    }

    @Test
    void testActualizarDesdeDTOMovieNotFound() {
        when(emisionRepository.findById(1L)).thenReturn(Optional.of(emision));
        when(movieRepository.findById(1)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> emisionService.actualizarDesdeDTO(1L, dto));

        assertEquals("Película no encontrada con ID: 1", ex.getMessage());
    }

    @Test
    void testActualizarDesdeDTORoomNotFound() {
        when(emisionRepository.findById(1L)).thenReturn(Optional.of(emision));
        when(movieRepository.findById(1)).thenReturn(Optional.of(movie));
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class,
                () -> emisionService.actualizarDesdeDTO(1L, dto));

        assertEquals("Sala no encontrada con ID: 1", ex.getMessage());
    }

    @Test
    void testEliminarOk() {
        when(emisionRepository.existsById(1L)).thenReturn(true);
        doNothing().when(emisionRepository).deleteById(1L);

        emisionService.eliminar(1L);
        verify(emisionRepository).deleteById(1L);
    }

    @Test
    void testEliminarNotFound() {
        when(emisionRepository.existsById(2L)).thenReturn(false);

        CustomException ex = assertThrows(CustomException.class, () -> emisionService.eliminar(2L));
        assertEquals("No se encontró la emisión con ID: 2", ex.getMessage());
    }

    @Test
    void testEmisionesPorRegion() {
        Emision emision = Emision.builder()
                .idEmision(1L)
                .movie(movie)
                .room(room)
                .fechaHoraInicio(LocalDateTime.now().plusHours(1))
                .idioma("Español")
                .estado(Emision.EstadoEmision.ACTIVO)
                .build();

        when(emisionRepository.findByRoom_Region_Id(1L)).thenReturn(List.of(emision));

        List<EmisionFrontDTO> result = emisionService.emisionesPorRegion(1L);

        assertEquals(1, result.size());
        EmisionFrontDTO dto = result.get(0);

        assertEquals("Inception", dto.pelicula());
        assertEquals("Room A", dto.sala());
        assertEquals("Español", dto.idioma());
    }

    @Test
    void testObtenerEmisionesPorSala() {
        when(emisionRepository.findByRoom_Idroom(1L)).thenReturn(List.of(emision));

        List<EmisionFrontDTO> result = emisionService.obtenerEmisionesPorSala(1L);

        assertEquals(1, result.size());
        EmisionFrontDTO dto = result.get(0);

        assertEquals("Inception", dto.pelicula());
        assertEquals("Room A", dto.sala());
        assertEquals("Español", dto.idioma());
    }

    @Test
    void testToEmisionResponseDTO() {
        EmisionResponseDTO responseDTO = emisionService.toEmisionResponseDTO(emision);

        assertEquals(emision.getIdEmision(), responseDTO.getIdEmision());
        assertEquals(emision.getMovie().getTitle(), responseDTO.getMovieTitle());
        assertEquals(emision.getRoom().getIdroom(), responseDTO.getRoomId());
        assertEquals(emision.getRoom().getNombreroom(), responseDTO.getRoomName());
        assertEquals(emision.getRoom().getCapacidad(), responseDTO.getRoomCapacity());
        assertEquals(emision.getIdioma(), responseDTO.getIdioma());
    }


}
