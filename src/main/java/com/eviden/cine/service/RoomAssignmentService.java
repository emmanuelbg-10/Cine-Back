package com.eviden.cine.service;

import com.eviden.cine.dtos.EmisionDTO;
import com.eviden.cine.exception.CustomException;
import com.eviden.cine.model.Emision;
import com.eviden.cine.model.Movie;
import com.eviden.cine.model.Room;
import com.eviden.cine.repository.EmisionRepository;
import com.eviden.cine.repository.MovieRepository;
import com.eviden.cine.repository.ReservationRepository;
import com.eviden.cine.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class RoomAssignmentService {

    private final RoomRepository roomRepository;
    private final EmisionRepository emisionRepository;
    private final MovieRepository movieRepository;
    private final ReservationRepository reservationRepository;

    @Autowired
    public RoomAssignmentService(RoomRepository roomRepository,
                                 EmisionRepository emisionRepository,
                                 MovieRepository movieRepository,
                                 ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.emisionRepository = emisionRepository;
        this.movieRepository = movieRepository;
        this.reservationRepository = reservationRepository;
    }

    public Room selectRoom(EmisionDTO dto) {
        Movie movie = movieRepository.findById(dto.getIdPelicula())
                .orElseThrow(() -> new CustomException("Película no encontrada con ID: " + dto.getIdPelicula()));

        if (esEstreno(dto, movie)) {
            return buscarSalaGrandeDisponible(dto.getFechaHoraInicio(), movie.getTime());
        } else {
            double ocupacion = calcularOcupacionHistorica((long) movie.getId());
            if (ocupacion > 70.0) {
                return buscarSalaGrandeDisponible(dto.getFechaHoraInicio(), movie.getTime());
            } else {
                return buscarSalaPequenaDisponible(dto.getFechaHoraInicio(), movie.getTime());
            }
        }
    }

    protected boolean esEstreno(EmisionDTO dto, Movie movie) {
        if (movie.getReleaseDate() == null) {
            throw new CustomException("La película no tiene fecha de estreno definida");
        }

        LocalDateTime fechaEmision = dto.getFechaHoraInicio();
        LocalDateTime fechaEstreno = movie.getReleaseDate().atStartOfDay(); // Inicio del día del estreno
        LocalDateTime fechaLimite = fechaEstreno.plusDays(7); // 7 días después

        return !fechaEmision.isBefore(fechaEstreno) && !fechaEmision.isAfter(fechaLimite);
    }

    protected double calcularOcupacionHistorica(Long movieId) {
        List<Emision> emisiones = emisionRepository.findByMovie_Id(movieId);

        if (emisiones.isEmpty()) {
            return 0.0;
        }

        int totalAsientos = 0;
        int totalReservados = 0;

        for (Emision emision : emisiones) {
            int capacidadSala = emision.getRoom().getCapacidad();
            int reservas = emision.getReservations() != null ? emision.getReservations().size() : 0;

            totalAsientos += capacidadSala;
            totalReservados += reservas;
        }

        if (totalAsientos == 0) {
            return 0.0;
        }

        return (totalReservados / (double) totalAsientos) * 100.0;
    }

    private Room buscarSalaGrandeDisponible(LocalDateTime fechaHoraInicio, Integer duracionPelicula) {
        return roomRepository.findAll().stream()
                .filter(room -> estaSalaDisponible(room, fechaHoraInicio, duracionPelicula))
                .max(Comparator.comparing(Room::getCapacidad))
                .orElseThrow(() -> new CustomException("No hay salas grandes disponibles para ese horario"));
    }

    private Room buscarSalaPequenaDisponible(LocalDateTime fechaHoraInicio, Integer duracionPelicula) {
        return roomRepository.findAll().stream()
                .filter(room -> estaSalaDisponible(room, fechaHoraInicio, duracionPelicula))
                .min(Comparator.comparing(Room::getCapacidad))
                .orElseThrow(() -> new CustomException("No hay salas pequeñas disponibles para ese horario"));
    }

    public boolean estaSalaDisponible(Room room, LocalDateTime nuevaHoraInicio, Integer duracionNueva) {
        List<Emision> emisiones = emisionRepository.findByRoom_Idroom(room.getIdroom());

        for (Emision emision : emisiones) {
            LocalDateTime inicioExistente = emision.getFechaHoraInicio();
            LocalDateTime finExistente = inicioExistente.plusMinutes((long) emision.getMovie().getTime() + 20L);

            LocalDateTime finNueva = nuevaHoraInicio.plusMinutes((long) duracionNueva + 20L);

            boolean seSolapan = nuevaHoraInicio.isBefore(finExistente) && inicioExistente.isBefore(finNueva);
            if (seSolapan) {
                return false;
            }
        }
        return true;
    }

}
