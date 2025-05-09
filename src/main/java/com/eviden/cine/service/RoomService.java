package com.eviden.cine.service;

import com.eviden.cine.dtos.*;
import com.eviden.cine.model.Asiento;
import com.eviden.cine.model.Region;
import com.eviden.cine.model.Room;
import com.eviden.cine.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final RegionService regionService;

    @Autowired
    public RoomService(RoomRepository roomRepository, RegionService regionService) {
        this.roomRepository = roomRepository;
        this.regionService = regionService;
    }

    public List<Room> obtenerTodasLasSalas() {
        return roomRepository.findAll();
    }

    public Optional<Room> obtenerSalaPorId(Long id) {
        return roomRepository.findById(id);
    }

    public Room crearDesdeDTO(RoomRequestDTO dto) {
        Region region = regionService.obtenerPorId(dto.getRegionId());
        Room room = Room.builder()
                .nombreroom(dto.getNombreroom())
                .filas(dto.getFilas())
                .columnas(dto.getColumnas())
                .region(region)
                .build();
        return guardarSala(room);
    }

    public Room actualizarDesdeDTO(Long id, RoomRequestDTO dto) {
        Room roomExistente = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala no encontrada"));

        // Actualizar solo los campos deseados
        roomExistente.setNombreroom(dto.getNombreroom());
        roomExistente.setFilas(dto.getFilas());
        roomExistente.setColumnas(dto.getColumnas());
        Region region = regionService.obtenerPorId(dto.getRegionId());
        roomExistente.setRegion(region);

        // No reemplazar la lista de emisiones, ya que las queremos mantener intactas
        return guardarSala(roomExistente);
    }

    public Room guardarSala(Room room) {
        if (room.getAsientos() == null || room.getAsientos().isEmpty()) {
            int filas = room.getFilas();
            int columnas = room.getColumnas();
            room.setCapacidad(filas * columnas);
            char filaFinal = (char) ('A' + filas - 1);
            for (char fila = 'A'; fila <= filaFinal; fila++) {
                for (int columna = 1; columna <= columnas; columna++) {
                    Asiento asiento = new Asiento();
                    asiento.setFila(String.valueOf(fila));
                    asiento.setColumna(columna);
                    asiento.setDisponible(true);
                    asiento.setTipoAsiento(tipoAsientoPorFila(fila));
                    room.agregarAsiento(asiento);
                }
            }
        }
        return roomRepository.save(room);
    }

    private String tipoAsientoPorFila(char fila) {
        return (fila == 'A') ? "minusvalido"
                : (fila == 'B') ? "VIP"
                : "normal";
    }

    public RoomResponseDTO toRoomResponseDTO(Room room) {
        return RoomResponseDTO.builder()
                .idroom(room.getIdroom())
                .nombreroom(room.getNombreroom())
                .filas(room.getFilas())
                .columnas(room.getColumnas())
                .regionId(room.getRegion().getId())
                .build();
    }

    public RoomAdminResponseDTO toRoomAdminResponseDTO(Room room) {
        System.out.println(">>> Procesando sala: " + room.getIdroom() + " - " + room.getNombreroom());

        room.getEmisiones().forEach(em -> {
            System.out.println(" - Emisión ID: " + em.getIdEmision());
            if (em.getMovie() == null) {
                System.out.println("   ⚠ Película es NULL para esta emisión");
            } else {
                System.out.println("   ✔ Película: " + em.getMovie().getTitle());
            }
        });
        return RoomAdminResponseDTO.builder()
                .idroom(room.getIdroom())
                .nombreroom(room.getNombreroom())
                .capacidad(room.getCapacidad())
                .filas(room.getFilas())
                .columnas(room.getColumnas())
                .regionName(room.getRegion().getName())
                .regionId(room.getRegion().getId())
                .emisiones(
                        room.getEmisiones().stream().map(em -> EmisionSimpleDTO.builder()
                                        .idEmision(em.getIdEmision())
                                        .idioma(em.getIdioma())
                                        .fechaHoraInicio(em.getFechaHoraInicio())
                                        .movie(em.getMovie() != null ? MovieSimpleDTO.builder()
                                                .id((long) em.getMovie().getId())
                                                .title(em.getMovie().getTitle())
                                                .time(em.getMovie().getTime())
                                                .build() : null)

                                        .build())
                                .collect(Collectors.toList())
                )
                .build();
    }

    public List<RoomAdminResponseDTO> obtenerSalasParaAdmin() {
        return roomRepository.findAll().stream()
                .map(this::toRoomAdminResponseDTO)
                .collect(Collectors.toList());
    }

    public void eliminarSala(Long id) {
        roomRepository.deleteById(id);
    }
}
