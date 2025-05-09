package com.eviden.cine.controller;

import com.eviden.cine.dtos.RoomRequestDTO;
import com.eviden.cine.dtos.RoomResponseDTO;
import com.eviden.cine.model.Room;
import com.eviden.cine.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class RoomControllerTest {

    private RoomController controller;
    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = mock(RoomService.class);
        controller = new RoomController(roomService);
    }

    @Test
    void testObtenerTodasLasRooms() {
        List<Room> rooms = List.of(new Room(), new Room());
        when(roomService.obtenerTodasLasSalas()).thenReturn(rooms);

        ResponseEntity<List<Room>> response = controller.obtenerTodasLasRooms();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(rooms, response.getBody());
        verify(roomService).obtenerTodasLasSalas();
    }

    @Test
    void testObtenerRoomPorId_Found() {
        Room room = new Room();
        when(roomService.obtenerSalaPorId(1L)).thenReturn(Optional.of(room));

        ResponseEntity<Room> response = controller.obtenerRoomPorId(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(room, response.getBody());
        verify(roomService).obtenerSalaPorId(1L);
    }

    @Test
    void testObtenerRoomPorId_NotFound() {
        when(roomService.obtenerSalaPorId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Room> response = controller.obtenerRoomPorId(1L);

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testCrearRoom() {
        RoomRequestDTO requestDTO = new RoomRequestDTO("Room Azul", 9, 12, 1L);

        Room room = new Room();
        RoomResponseDTO responseDTO = new RoomResponseDTO(1L, "Room Azul", 9, 12, 1L);

        when(roomService.crearDesdeDTO(requestDTO)).thenReturn(room);
        when(roomService.toRoomResponseDTO(room)).thenReturn(responseDTO);

        ResponseEntity<RoomResponseDTO> response = controller.crearRoom(requestDTO);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseDTO, response.getBody());
    }

    @Test
    void testActualizarRoom_Found() {
        Long roomId = 1L;
        RoomRequestDTO requestDTO = new RoomRequestDTO("Room Verde Actualizada", 10, 12, 1L);

        Room room = new Room();
        RoomResponseDTO responseDTO = new RoomResponseDTO(1L, "Room Verde Actualizada", 10, 12, 1L);

        when(roomService.obtenerSalaPorId(roomId)).thenReturn(Optional.of(new Room()));
        when(roomService.actualizarDesdeDTO(roomId, requestDTO)).thenReturn(room);
        when(roomService.toRoomResponseDTO(room)).thenReturn(responseDTO);

        ResponseEntity<RoomResponseDTO> response = controller.actualizarRoom(roomId, requestDTO);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(responseDTO, response.getBody());
    }

    @Test
    void testActualizarRoom_NotFound() {
        Long roomId = 2L;
        RoomRequestDTO requestDTO = new RoomRequestDTO("Room Inexistente", 10, 12, 1L);

        when(roomService.obtenerSalaPorId(roomId)).thenReturn(Optional.empty());

        ResponseEntity<RoomResponseDTO> response = controller.actualizarRoom(roomId, requestDTO);

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testEliminarRoom() {
        doNothing().when(roomService).eliminarSala(1L);

        ResponseEntity<Void> response = controller.eliminarRoom(1L);

        assertEquals(204, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(roomService).eliminarSala(1L);
    }
}
