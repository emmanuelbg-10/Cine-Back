package com.eviden.cine.service;

import com.eviden.cine.dtos.EmisionDTO;
import com.eviden.cine.exception.CustomException;
import com.eviden.cine.model.Emision;
import com.eviden.cine.model.Movie;
import com.eviden.cine.model.Reservation;
import com.eviden.cine.model.Room;
import com.eviden.cine.repository.EmisionRepository;
import com.eviden.cine.repository.MovieRepository;
import com.eviden.cine.repository.ReservationRepository;
import com.eviden.cine.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoomAssignmentServiceTest {

    private RoomRepository roomRepository;
    private EmisionRepository emisionRepository;
    private MovieRepository movieRepository;
    private ReservationRepository reservationRepository;
    private RoomAssignmentService service;

    @BeforeEach
    void setup() {
        roomRepository = mock(RoomRepository.class);
        emisionRepository = mock(EmisionRepository.class);
        movieRepository = mock(MovieRepository.class);
        reservationRepository = mock(ReservationRepository.class);

        service = new RoomAssignmentService(roomRepository, emisionRepository, movieRepository, reservationRepository);
    }

    @Test
    void testEsEstreno_true() {
        Movie movie = new Movie();
        movie.setReleaseDate(LocalDate.of(2025, 5, 1));

        EmisionDTO dto = new EmisionDTO(1, null, LocalDateTime.of(2025, 5, 3, 18, 0), "ES", Emision.EstadoEmision.ACTIVO);

        boolean result = service.esEstreno(dto, movie);
        assertTrue(result);
    }

    @Test
    void testEsEstreno_false() {
        Movie movie = new Movie();
        movie.setReleaseDate(LocalDate.of(2025, 5, 1));

        EmisionDTO dto = new EmisionDTO(1, null, LocalDateTime.of(2025, 5, 15, 18, 0), "ES", Emision.EstadoEmision.ACTIVO);

        boolean result = service.esEstreno(dto, movie);
        assertFalse(result);
    }

    @Test
    void testEstaSalaDisponible_trueWhenNoOverlap() {
        Room room = new Room();
        room.setIdroom(1L);

        Movie movie = new Movie();
        movie.setTime(100);

        Emision emision = new Emision();
        emision.setMovie(movie);
        emision.setFechaHoraInicio(LocalDateTime.of(2025, 5, 10, 10, 0));
        emision.setRoom(room);

        when(emisionRepository.findByRoom_Idroom(1L)).thenReturn(List.of(emision));

        boolean result = service.estaSalaDisponible(room, LocalDateTime.of(2025, 5, 10, 13, 0), 90);
        assertTrue(result);
    }

    @Test
    void testEstaSalaDisponible_falseWhenOverlap() {
        Room room = new Room();
        room.setIdroom(1L);

        Movie movie = new Movie();
        movie.setTime(120);

        Emision emision = new Emision();
        emision.setMovie(movie);
        emision.setFechaHoraInicio(LocalDateTime.of(2025, 5, 10, 10, 0));
        emision.setRoom(room);

        when(emisionRepository.findByRoom_Idroom(1L)).thenReturn(List.of(emision));

        boolean result = service.estaSalaDisponible(room, LocalDateTime.of(2025, 5, 10, 11, 0), 90);
        assertFalse(result);
    }

    @Test
    void testCalcularOcupacionHistorica_withReservations() {
        Room room = new Room();
        room.setCapacidad(100);

        Movie movie = new Movie();
        movie.setTime(100);

        Emision emision = new Emision();
        emision.setRoom(room);
        emision.setMovie(movie);
        emision.setReservations(List.of(new Reservation(), new Reservation(), new Reservation()));

        when(emisionRepository.findByMovie_Id(1L)).thenReturn(List.of(emision));

        double ocupacion = service.calcularOcupacionHistorica(1L);
        assertEquals(3.0, ocupacion, 0.01); // 3%
    }

    @Test
    void testSelectRoom_throwsIfNoRoomsAvailable() {
        EmisionDTO dto = new EmisionDTO(99, null, LocalDateTime.now(), "ES", Emision.EstadoEmision.ACTIVO);
        Movie movie = new Movie();
        movie.setId(99);
        movie.setTime(100);
        movie.setReleaseDate(LocalDate.now());

        when(movieRepository.findById(99)).thenReturn(Optional.of(movie));
        when(roomRepository.findAll()).thenReturn(List.of());

        assertThrows(CustomException.class, () -> service.selectRoom(dto));
    }
}
