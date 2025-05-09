package com.eviden.cine.service;

import com.eviden.cine.dtos.ForoRequestDTO;
import com.eviden.cine.dtos.ForoResponseDTO;
import com.eviden.cine.model.Foro;
import com.eviden.cine.model.Movie;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.ForoRepository;
import com.eviden.cine.repository.MovieRepository;
import com.eviden.cine.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ForoService {

    private final ForoRepository foroRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    public List<ForoResponseDTO> getAllForos() {
        List<Foro> foros = foroRepository.findAll();
        return foros.stream()
                .map(foro -> ForoResponseDTO.builder()
                        .id(foro.getId())
                        .title(foro.getTitle())
                        .description(foro.getDescription())
                        .movieTitle(foro.getMovie().getTitle())
                        .ownerEmail(foro.getOwner().getEmail())
                        .createdAt(foro.getCreatedAt())
                        .build())
                .toList();
    }

    public ForoResponseDTO getForoById(Long id){
        Foro foro = foroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Foro no encontrado"));
        return ForoResponseDTO.builder()
                .id(foro.getId())
                .title(foro.getTitle())
                .description(foro.getDescription())
                .movieTitle(foro.getMovie().getTitle())
                .ownerEmail(foro.getOwner().getEmail())
                .createdAt(foro.getCreatedAt())
                .build();
    }

    public Foro createForo(ForoRequestDTO dto) {
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new EntityNotFoundException("Película no encontrada con ID: " + dto.getMovieId()));
        User owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + dto.getOwnerId()));

        Foro foro = Foro.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .movie(movie)
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .build();

        return foroRepository.save(foro);
    }

    public Foro updateForo(Long id, ForoRequestDTO dto) {
        Foro foro = foroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Foro no encontrado"));

        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción no puede estar vacía");
        }

        foro.setTitle(dto.getTitle().trim());
        foro.setDescription(dto.getDescription().trim());

        return foroRepository.save(foro);
    }

    public void deleteForoById(Long id) {
        foroRepository.deleteById(id);
    }
}
