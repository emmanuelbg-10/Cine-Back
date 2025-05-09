package com.eviden.cine.controller;

import com.eviden.cine.dtos.ForoRequestDTO;
import com.eviden.cine.dtos.ForoResponseDTO;
import com.eviden.cine.model.Foro;
import com.eviden.cine.service.ForoService;
import com.eviden.cine.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/foro")
public class ForoController {

    private final ForoService foroService;
    private final MessageService messageService;

    @GetMapping("/list")
    public ResponseEntity<List<ForoResponseDTO>> listarForos() {
        return ResponseEntity.ok(foroService.getAllForos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ForoResponseDTO> getForoById(@PathVariable Long id){
        return ResponseEntity.ok(foroService.getForoById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<ForoResponseDTO> crearForo(@RequestBody ForoRequestDTO foro) {
        Foro foroCreado = foroService.createForo(foro);
        ForoResponseDTO foroResponse = ForoResponseDTO.builder()
                .id(foroCreado.getId())
                .title(foroCreado.getTitle())
                .description(foroCreado.getDescription())
                .movieTitle(foroCreado.getMovie().getTitle())
                .ownerEmail(foroCreado.getOwner().getEmail())
                .createdAt(foroCreado.getCreatedAt())
                .build();
        return ResponseEntity.ok(foroResponse);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ForoResponseDTO> updateForo(@PathVariable Long id, @RequestBody ForoRequestDTO foro) {
        Foro foroActualizado = foroService.updateForo(id, foro);
        ForoResponseDTO foroResponse = ForoResponseDTO.builder()
                .id(foroActualizado.getId())
                .title(foroActualizado.getTitle())
                .description(foroActualizado.getDescription())
                .movieTitle(foroActualizado.getMovie().getTitle())
                .ownerEmail(foroActualizado.getOwner().getEmail())
                .createdAt(foroActualizado.getCreatedAt())
                .build();
        return ResponseEntity.ok(foroResponse);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteForo(@PathVariable Long id) {
        foroService.deleteForoById(id);
        return ResponseEntity.noContent().build();
    }

}
