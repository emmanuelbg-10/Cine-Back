package com.eviden.cine.service;

import com.eviden.cine.dtos.RoomAdminResponseDTO;
import com.eviden.cine.dtos.RoomRequestDTO;
import com.eviden.cine.dtos.RoomResponseDTO;
import com.eviden.cine.model.Emision;
import com.eviden.cine.model.Movie;
import com.eviden.cine.model.Region;
import com.eviden.cine.model.Room;
import com.eviden.cine.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RegionService regionService;

    @InjectMocks
    private RoomService roomService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testObtenerTodasLasSalas() {
        List<Room> mockRooms = List.of(new Room());
        when(roomRepository.findAll()).thenReturn(mockRooms);

        List<Room> result = roomService.obtenerTodasLasSalas();
        assertEquals(1, result.size());
    }

    @Test
    void testObtenerSalaPorId() {
        Room room = new Room();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        Optional<Room> result = roomService.obtenerSalaPorId(1L);
        assertTrue(result.isPresent());
    }

    @Test
    void testCrearDesdeDTO() {
        RoomRequestDTO dto = new RoomRequestDTO("Sala 1", 5, 5, 1L);
        Region region = new Region();
        when(regionService.obtenerPorId(1L)).thenReturn(region);
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));

        Room result = roomService.crearDesdeDTO(dto);
        assertEquals("Sala 1", result.getNombreroom());
        assertEquals(25, result.getCapacidad());
        assertFalse(result.getAsientos().isEmpty());
    }

    // src/test/java/com/eviden/cine/service/RoomServiceTest.java
    @Test
    void testActualizarDesdeDTO() {
        RoomRequestDTO dto = new RoomRequestDTO("Sala Actualizada", 3, 4, 2L);
        Region region = new Region();
        when(regionService.obtenerPorId(2L)).thenReturn(region);

        // Simular que la sala existe
        Room roomExistente = new Room();
        roomExistente.setIdroom(99L);
        // Valores originales (no afectan la capacidad, ya que se recalcula)
        roomExistente.setFilas(2);
        roomExistente.setColumnas(2);
        roomExistente.setNombreroom("Sala Original");
        when(roomRepository.findById(99L)).thenReturn(Optional.of(roomExistente));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));

        Room result = roomService.actualizarDesdeDTO(99L, dto);
        assertEquals("Sala Actualizada", result.getNombreroom());
        assertEquals(12, result.getCapacidad());
    }

    @Test
    void testGuardarSala_creaAsientos() {
        Room room = Room.builder().filas(2).columnas(3).build();
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));

        Room result = roomService.guardarSala(room);
        assertEquals(6, result.getAsientos().size());
        assertEquals(6, result.getCapacidad());
        assertEquals("minusvalido", result.getAsientos().get(0).getTipoAsiento());
    }

    @Test
    void testToRoomResponseDTO() {
        Region region = new Region();
        region.setId(10L);
        Room room = Room.builder()
                .idroom(1L)
                .nombreroom("Sala Test")
                .filas(5)
                .columnas(6)
                .region(region)
                .build();

        RoomResponseDTO dto = roomService.toRoomResponseDTO(room);
        assertEquals("Sala Test", dto.getNombreroom());
        assertEquals(10L, dto.getRegionId());
    }

    @Test
    void testToRoomAdminResponseDTO() {
        Region region = new Region();
        region.setId(1L);
        region.setName("Centro");

        Movie movie = new Movie();
        movie.setId(101);
        movie.setTitle("Película");
        movie.setTime(120);

        Emision emision = Emision.builder()
                .idEmision(55L)
                .idioma("es")
                .fechaHoraInicio(LocalDateTime.now())
                .movie(movie)
                .build();

        Room room = Room.builder()
                .idroom(1L)
                .nombreroom("Sala 1")
                .capacidad(100)
                .filas(5)
                .columnas(20)
                .region(region)
                .emisiones(List.of(emision))
                .build();

        RoomAdminResponseDTO dto = roomService.toRoomAdminResponseDTO(room);
        assertEquals("Sala 1", dto.getNombreroom());
        assertEquals("Centro", dto.getRegionName());
        assertEquals(1, dto.getEmisiones().size());
        assertEquals("Película", dto.getEmisiones().get(0).getMovie().getTitle());
    }

    @Test
    void testObtenerSalasParaAdmin() {
        Room room = new Room();
        room.setNombreroom("Sala Admin");
        room.setRegion(new Region());
        room.setEmisiones(Collections.emptyList());

        when(roomRepository.findAll()).thenReturn(List.of(room));

        List<RoomAdminResponseDTO> result = roomService.obtenerSalasParaAdmin();
        assertEquals(1, result.size());
        assertEquals("Sala Admin", result.get(0).getNombreroom());
    }

    @Test
    void testEliminarSala() {
        roomService.eliminarSala(1L);
        verify(roomRepository).deleteById(1L);
    }
}
