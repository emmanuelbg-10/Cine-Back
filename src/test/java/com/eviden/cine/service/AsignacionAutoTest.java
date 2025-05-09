package com.eviden.cine.service;

import com.eviden.cine.model.Emision;
import com.eviden.cine.model.Movie;
import com.eviden.cine.model.Room;
import com.eviden.cine.repository.EmisionRepository;
import com.eviden.cine.repository.MovieRepository;
import com.eviden.cine.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AsignacionAutoTest {

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

    private Room room;
    private Movie movie;
    private Emision emision;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        room = Room.builder()
                .idroom(1L)
                .nombreroom("Sala Test")
                .capacidad(100)
                .build();

        movie = Movie.builder()
                .id(1)
                .title("Película Test")
                .releaseDate(LocalDate.now().minusDays(2)) // estreno
                .time(100)
                .isAvailable(true)
                .isComingSoon(false)
                .build();

        emision = Emision.builder()
                .idEmision(1L)
                .fechaHoraInicio(LocalDateTime.now())
                .movie(movie)
                .room(room) // Asignación correcta de la sala
                .build();

        when(movieRepository.findAll()).thenReturn(List.of(movie));
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(roomAssignmentService.selectRoom(any())).thenReturn(room);
        // Importantísimo: simular disponibilidad limitada
        AtomicInteger contador = new AtomicInteger(0);
        when(roomAssignmentService.estaSalaDisponible(any(Room.class), any(LocalDateTime.class), anyInt()))
                .thenAnswer(invocation -> {
                    int count = contador.getAndIncrement();
                    return count < 5; // solo permite 5 emisiones, luego dice "no disponible"
                });

        when(emisionRepository.findByRoom_Idroom(anyLong())).thenReturn(List.of());
    }

    @Test
    void generarEmisionesAutomaticas_deberiaCrearEmisionesLimitadas() {
        // Ejecutar
        long start = System.currentTimeMillis();
        emisionService.generarEmisionesAutomaticas();
        long duration = System.currentTimeMillis() - start;

        // Capturar emisiones guardadas
        ArgumentCaptor<Emision> captor = ArgumentCaptor.forClass(Emision.class);
        verify(emisionRepository, atLeastOnce()).save(captor.capture());

        List<Emision> emisionesGuardadas = captor.getAllValues();

        // Validar
        assertThat(emisionesGuardadas).isNotEmpty();
        assertThat(emisionesGuardadas.size()).isLessThanOrEqualTo(50);

        emisionesGuardadas.forEach(emision -> {
            assertThat(emision.getRoom()).isEqualTo(room);
            assertThat(emision.getMovie().getTitle()).isEqualTo(movie.getTitle());
            assertThat(emision.getFechaHoraInicio().getHour()).isBetween(11, 23);
        });

        // Verificar que no se haya excedido tiempo razonable
        assertThat(duration).isLessThan(5000); // menos de 5 segundos

        // Mostrar en consola
        emisionesGuardadas.forEach(emision ->
                System.out.println("✅ Emisión: " + emision.getMovie().getTitle() +
                        " en sala " + emision.getRoom().getNombreroom() +
                        " el " + emision.getFechaHoraInicio().toLocalDate() +
                        " a las " + emision.getFechaHoraInicio().toLocalTime())
        );
    }
}
