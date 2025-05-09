package com.eviden.cine.controller;

import com.eviden.cine.model.Director;
import com.eviden.cine.repository.DirectorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorRepository directorRepository;

    @GetMapping("/search")
    @Operation(summary = "Buscar directores", description = "Busca directores por nombre")
    public ResponseEntity<List<Director>> searchDirectors(@Parameter(description = "Nombre del director") @RequestParam String name) {
        return ResponseEntity.ok(directorRepository.findByNameContainingIgnoreCase(name));
    }
}