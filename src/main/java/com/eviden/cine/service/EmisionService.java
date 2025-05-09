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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@EnableScheduling
@Service
public class EmisionService {

    private static final Logger logger = LoggerFactory.getLogger(EmisionService.class);
    private static final SecureRandom random = new SecureRandom();


    private final EmisionRepository emisionRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final RoomAssignmentService roomAssignmentService;

    @Autowired
    public EmisionService(
            EmisionRepository emisionRepository,
            MovieRepository movieRepository,
            RoomRepository roomRepository,
            RoomAssignmentService roomAssignmentService
    ) {
        this.emisionRepository = emisionRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.roomAssignmentService = roomAssignmentService;
    }

    public List<Emision> obtenerTodas() {
        return emisionRepository.findAll().stream()
                .filter(em -> em.getFechaHoraInicio().isAfter(LocalDateTime.now()))
                .toList();
    }

    public Optional<Emision> obtenerPorId(Long id) {
        return emisionRepository.findById(id);
    }

    public Emision guardarDesdeDTO(EmisionDTO dto) {
        Movie movie = movieRepository.findById(dto.getIdPelicula())
                .orElseThrow(() -> new CustomException("Película no encontrada con ID: " + dto.getIdPelicula()));

        Room room;
        if (dto.getIdRoom() != null) {
            room = roomRepository.findById(dto.getIdRoom())
                    .orElseThrow(() -> new CustomException("Sala no encontrada con ID: " + dto.getIdRoom()));
            if (!roomAssignmentService.estaSalaDisponible(room, dto.getFechaHoraInicio(), movie.getTime())) {
                throw new CustomException("La sala seleccionada no está disponible en ese horario");
            }
        } else {
            room = roomAssignmentService.selectRoom(dto);
        }

        Emision emision = Emision.builder()
                .movie(movie)
                .room(room)
                .fechaHoraInicio(dto.getFechaHoraInicio())
                .idioma(dto.getIdioma())
                .estado(dto.getEstado())
                .build();

        return emisionRepository.save(emision);
    }

    public Emision actualizarDesdeDTO(Long id, EmisionDTO dto) {
        return emisionRepository.findById(id).map(emision -> {
            Movie movie = movieRepository.findById(dto.getIdPelicula())
                    .orElseThrow(() -> new CustomException("Película no encontrada con ID: " + dto.getIdPelicula()));

            Room room;
            if (dto.getIdRoom() != null) {
                room = roomRepository.findById(dto.getIdRoom())
                        .orElseThrow(() -> new CustomException("Sala no encontrada con ID: " + dto.getIdRoom()));
                if (!roomAssignmentService.estaSalaDisponible(room, dto.getFechaHoraInicio(), movie.getTime())) {
                    throw new CustomException("La sala seleccionada no está disponible en ese horario");
                }
            } else {
                room = roomAssignmentService.selectRoom(dto);
            }

            emision.setMovie(movie);
            emision.setRoom(room);
            emision.setFechaHoraInicio(dto.getFechaHoraInicio());
            emision.setIdioma(dto.getIdioma());
            emision.setEstado(dto.getEstado());

            return emisionRepository.save(emision);
        }).orElseThrow(() -> new CustomException("Emisión no encontrada con ID: " + id));
    }

    public void eliminar(Long id) {
        if (!emisionRepository.existsById(id)) {
            throw new CustomException("No se encontró la emisión con ID: " + id);
        }
        emisionRepository.deleteById(id);
    }

    public List<EmisionFrontDTO> emisionesPorRegion(Long regionId) {
        return emisionRepository.findByRoom_Region_Id(regionId).stream()
                .filter(em -> em.getFechaHoraInicio().isAfter(LocalDateTime.now()))
                .map(EmisionFrontDTO::of)
                .toList();
    }


    public List<Emision> getEmisionesByMovieId(Long id) {
        return emisionRepository.findByMovie_Id(id).stream()
                .filter(em -> em.getFechaHoraInicio().isAfter(LocalDateTime.now()))
                .toList();
    }

    public List<EmisionFrontDTO> obtenerEmisionesPorSala(Long roomId) {
        return emisionRepository.findByRoom_Idroom(roomId).stream()
                .filter(em -> em.getFechaHoraInicio().isAfter(LocalDateTime.now()))
                .map(EmisionFrontDTO::of)
                .toList();
    }


    public EmisionResponseDTO toEmisionResponseDTO(Emision emision) {
        return EmisionResponseDTO.builder()
                .idEmision(emision.getIdEmision())
                .movieTitle(emision.getMovie().getTitle())
                .roomId(emision.getRoom().getIdroom())
                .roomName(emision.getRoom().getNombreroom())
                .roomCapacity(emision.getRoom().getCapacidad())
                .fechaHoraInicio(emision.getFechaHoraInicio())
                .idioma(emision.getIdioma())
                .estado(emision.getEstado())
                .build();
    }

    @Scheduled(cron = "0 0 3 * * ?") // Todos los días a las 3:00 AM
    public void generarEmisionesAutomaticas() {
        List<Movie> peliculas = movieRepository.findAll().stream()
                .filter(movie -> movie.isAvailable() || movie.isComingSoon())
                .toList();

        if (peliculas.isEmpty()) {
            logger.warn("⚠️ No hay películas disponibles para generar emisiones.");
            return;
        }

        List<Room> salas = roomRepository.findAll();

        LocalDateTime inicioSemana = LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0).withNano(0); // Hoy

        for (Room sala : salas) {
            for (int dia = 0; dia < 7; dia++) {
                LocalDateTime horaActual = inicioSemana.plusDays(dia).withHour(13); // 13:00 cada día

                while (horaActual.getHour() < 24) {
                    Movie pelicula = seleccionarPeliculaDisponible(peliculas, sala, horaActual);
                    if (pelicula == null) {
                        logger.warn("🛑 Sin películas disponibles para sala '{}' a las {}", sala.getNombreroom(), horaActual);
                        horaActual = horaActual.plusMinutes(30); // Reintenta en otro slot
                        continue;
                    }

                    int duracionConLimpieza = pelicula.getTime() + 20;
                    LocalDateTime fin = horaActual.plusMinutes(duracionConLimpieza);
                    if (fin.isAfter(horaActual.toLocalDate().atTime(23, 59))) break;

                    EmisionDTO dto = EmisionDTO.builder()
                            .idPelicula(pelicula.getId())
                            .fechaHoraInicio(horaActual)
                            .idioma("Español")
                            .estado(Emision.EstadoEmision.ACTIVO)
                            .build();

                    try {
                        if (!roomAssignmentService.estaSalaDisponible(sala, horaActual, pelicula.getTime())) {
                            horaActual = horaActual.plusMinutes(30);
                            continue;
                        }

                        Emision nuevaEmision = Emision.builder()
                                .movie(pelicula)
                                .room(sala) // ⬅️ Usa directamente la sala del bucle
                                .fechaHoraInicio(horaActual)
                                .idioma("Español")
                                .estado(Emision.EstadoEmision.ACTIVO)
                                .build();

                        emisionRepository.save(nuevaEmision);

                        logger.info("📅 Día {} ✅ Emisión: '{}' en '{}' a las {}",
                                dia + 1, pelicula.getTitle(), sala.getNombreroom(), horaActual);

                        horaActual = horaActual.plusMinutes(duracionConLimpieza);

                    } catch (Exception ex) {
                        logger.warn("⚠️ No se pudo generar emisión en '{}': {}", sala.getNombreroom(), ex.getMessage());
                        horaActual = horaActual.plusMinutes(30);
                    }

                }
            }
        }
    }

    private Movie seleccionarPeliculaDisponible(List<Movie> peliculas, Room sala, LocalDateTime hora) {
        List<Movie> candidatas = peliculas.stream()
                .filter(movie -> {
                    List<Emision> emisionesSala = emisionRepository.findByRoom_Idroom(sala.getIdroom());

                    boolean haySolape = emisionesSala.stream().anyMatch(emision -> {
                        LocalDateTime inicio = emision.getFechaHoraInicio();
                        LocalDateTime fin = inicio.plusMinutes((long) emision.getMovie().getTime() + 20L);
                        LocalDateTime nuevaFin = hora.plusMinutes((long) movie.getTime() + 20L);
                        return hora.isBefore(fin) && inicio.isBefore(nuevaFin);
                    });


                    if (haySolape) return false;

                    boolean yaProgramadaEseDia = emisionesSala.stream().anyMatch(emision ->
                            emision.getFechaHoraInicio().toLocalDate().equals(hora.toLocalDate()) &&
                                    emision.getMovie().getId() == movie.getId()
                    );

                    return !yaProgramadaEseDia;
                })
                .toList();

        if (candidatas.isEmpty()) return null;

        List<Movie> estrenos = candidatas.stream()
                .filter(movie -> movie.getReleaseDate() != null &&
                        !movie.getReleaseDate().isBefore(LocalDateTime.now().toLocalDate().minusDays(14)))
                .toList();

        if (!estrenos.isEmpty()) {
            return estrenos.get(random.nextInt(estrenos.size()));
        }

        return candidatas.get(random.nextInt(candidatas.size()));
    }




}