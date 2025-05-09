package com.eviden.cine.service;

import com.eviden.cine.exception.CustomException;
import com.eviden.cine.model.Asiento;
import com.eviden.cine.model.Room;
import com.eviden.cine.repository.AsientoRepository;
import com.eviden.cine.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AsientoServiceTest {

    @Mock
    private AsientoRepository asientoRepository;

    @Mock
    private RoomRepository salaRepository;

    @InjectMocks
    private AsientoService asientoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testObtenerTodosLosAsientos() {
        Asiento a1 = new Asiento();
        Asiento a2 = new Asiento();
        when(asientoRepository.findAll()).thenReturn(List.of(a1, a2));

        List<Asiento> asientos = asientoService.obtenerTodosLosAsientos();

        assertEquals(2, asientos.size());
    }

    @Test
    void testObtenerAsientoPorIdEncontrado() {
        Asiento asiento = new Asiento();
        asiento.setIdAsiento(1L);

        when(asientoRepository.findById(1L)).thenReturn(Optional.of(asiento));

        Optional<Asiento> result = asientoService.obtenerAsientoPorId(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getIdAsiento());
    }

    @Test
    void testObtenerAsientoPorIdNoEncontrado() {
        when(asientoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Asiento> result = asientoService.obtenerAsientoPorId(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void testObtenerAsientosPorSala() {
        Asiento a1 = new Asiento();
        Asiento a2 = new Asiento();

        when(asientoRepository.findByroomIdroom(10L)).thenReturn(List.of(a1, a2));

        List<Asiento> asientos = asientoService.obtenerAsientosPorroom(10L);

        assertEquals(2, asientos.size());
    }

    @Test
    void testGuardarAsiento() {
        Room room = new Room();
        room.setIdroom(1L);

        Asiento asiento = new Asiento();
        asiento.setFila("B");
        asiento.setColumna(5);
        asiento.setRoom(room);

        when(salaRepository.existsById(1L)).thenReturn(true);
        when(asientoRepository.save(asiento)).thenReturn(asiento);

        Asiento result = asientoService.guardarAsiento(asiento);

        assertNotNull(result);
        assertEquals("B", result.getFila());
        assertEquals(5, result.getColumna());
        assertEquals(room, result.getRoom());
    }

    @Test
    void testGuardarAsientoRoomNoExiste() {
        Room room = new Room();
        room.setIdroom(999L);

        Asiento asiento = new Asiento();
        asiento.setRoom(room);

        when(salaRepository.existsById(999L)).thenReturn(false);

        CustomException ex = assertThrows(CustomException.class, () -> {
            asientoService.guardarAsiento(asiento);
        });

        assertEquals("La sala con ID 999 no existe.", ex.getMessage());
    }

    @Test
    void testEliminarAsiento() {
        asientoService.eliminarAsiento(1L);
        verify(asientoRepository).deleteById(1L);
    }

    @Test
    void testCambiarDisponibilidad() {
        List<Long> ids = List.of(1L, 2L);

        Asiento asiento1 = new Asiento();
        asiento1.setIdAsiento(1L);
        asiento1.setDisponible(true);

        Asiento asiento2 = new Asiento();
        asiento2.setIdAsiento(2L);
        asiento2.setDisponible(true);

        List<Asiento> asientosMock = List.of(asiento1, asiento2);

        when(asientoRepository.findAllById(ids)).thenReturn(asientosMock);
        when(asientoRepository.saveAll(asientosMock)).thenReturn(asientosMock);

        List<Asiento> resultado = asientoService.cambiarDisponibilidad(ids);

        assertEquals(2, resultado.size());
        assertFalse(resultado.get(0).isDisponible());
        assertFalse(resultado.get(1).isDisponible());

        verify(asientoRepository).findAllById(ids);
        verify(asientoRepository).saveAll(asientosMock);
    }

    @Test
    void testCambiarDisponibilidadFail() {
        List<Long> ids = List.of(1L, 2L);
        List<Asiento> soloUno = List.of(new Asiento());

        when(asientoRepository.findAllById(ids)).thenReturn(soloUno);

        CustomException ex = assertThrows(CustomException.class, () -> {
            asientoService.cambiarDisponibilidad(ids);
        });

        assertEquals("Uno o más asientos no fueron encontrados.", ex.getMessage());
    }

    // NUEVOS TESTS 👇👇👇

    @Test
    void testIsAsientoAvailable_AllAvailable() {
        Asiento a1 = new Asiento();
        a1.setDisponible(true);
        Asiento a2 = new Asiento();
        a2.setDisponible(true);

        List<Long> ids = List.of(1L, 2L);
        when(asientoRepository.findAllById(ids)).thenReturn(List.of(a1, a2));

        boolean result = asientoService.isAsientoAvailable(ids);

        assertTrue(result);
    }

    @Test
    void testIsAsientoAvailable_MissingSeats_ThrowsIllegalArgumentException() {
        List<Long> ids = List.of(1L, 2L);
        List<Asiento> soloUno = List.of(new Asiento());

        when(asientoRepository.findAllById(ids)).thenReturn(soloUno);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> asientoService.isAsientoAvailable(ids));
        assertEquals("Uno o más asientos no fueron encontrados", ex.getMessage());
    }

    @Test
    void testIsAsientoAvailable_NotAllAvailable_ThrowsIllegalStateException() {
        Asiento a1 = new Asiento();
        a1.setDisponible(true);
        Asiento a2 = new Asiento();
        a2.setDisponible(false);

        List<Long> ids = List.of(1L, 2L);
        when(asientoRepository.findAllById(ids)).thenReturn(List.of(a1, a2));

        Exception ex = assertThrows(IllegalStateException.class, () -> asientoService.isAsientoAvailable(ids));
        assertEquals("Uno o más asientos no están disponibles", ex.getMessage());
    }
}
