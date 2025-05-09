package com.eviden.cine.controller;

import com.eviden.cine.dtos.RoomAdminResponseDTO;
import com.eviden.cine.dtos.RoomRequestDTO;
import com.eviden.cine.dtos.RoomResponseDTO;
import com.eviden.cine.model.Room;
import com.eviden.cine.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Rooms", description = "Operaciones para la gestión de salas de cine")
public class RoomController {

    private final RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @Operation(summary = "Obtener todas las salas registradas")
    @ApiResponse(responseCode = "200", description = "Listado de salas obtenido correctamente",
            content = @Content(mediaType = "application/json"))
    @GetMapping
    public ResponseEntity<List<Room>> obtenerTodasLasRooms() {
        return ResponseEntity.ok(roomService.obtenerTodasLasSalas());
    }

    @Operation(summary = "Obtener una sala por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sala encontrada", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Sala no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Room> obtenerRoomPorId(
            @Parameter(description = "ID de la sala a consultar") @PathVariable Long id) {
        Optional<Room> room = roomService.obtenerSalaPorId(id);
        return room.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear una nueva sala")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sala creada correctamente con asientos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoomResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Error en los datos enviados", content = @Content)
    })
    @PostMapping
    public ResponseEntity<RoomResponseDTO> crearRoom(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "DTO para crear una sala",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RoomRequestDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "nombreroom": "Room Azul",
                                  "filas": 9,
                                  "columnas": 12,
                                  "regionId": 1
                                }
                            """)
                    )
            )
            @Valid @RequestBody RoomRequestDTO requestDTO) {
        Room room = roomService.crearDesdeDTO(requestDTO);
        return ResponseEntity.ok(roomService.toRoomResponseDTO(room));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar una sala existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sala actualizada correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoomResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Sala no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<RoomResponseDTO> actualizarRoom(
            @Parameter(description = "ID de la sala a actualizar") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "DTO con los datos actualizados",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RoomRequestDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "nombreroom": "Room Verde Actualizada",
                                  "filas": 10,
                                  "columnas": 12,
                                  "regionId": 1
                                }
                            """)
                    )
            )
            @Valid @RequestBody RoomRequestDTO requestDTO) {
        Optional<Room> roomExistente = roomService.obtenerSalaPorId(id);
        if (roomExistente.isPresent()) {
            Room room = roomService.actualizarDesdeDTO(id, requestDTO);
            return ResponseEntity.ok(roomService.toRoomResponseDTO(room));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar una sala por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sala eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Sala no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRoom(
            @Parameter(description = "ID de la sala a eliminar") @PathVariable Long id) {
        roomService.eliminarSala(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener todas las salas con información de emisiones (para administración)")
    @ApiResponse(responseCode = "200", description = "Listado detallado de salas para gestión",
            content = @Content(mediaType = "application/json"))
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoomAdminResponseDTO>> obtenerRoomsParaAdmin() {
        return ResponseEntity.ok(roomService.obtenerSalasParaAdmin());
    }

}
