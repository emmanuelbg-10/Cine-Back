package com.eviden.cine.controller;

import com.eviden.cine.model.Actor;
import com.eviden.cine.repository.ActorRepository;
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
@RequestMapping("/api/actors")
@RequiredArgsConstructor
public class CastingController {
    private final ActorRepository actorRepository;

    @GetMapping("/search")
    @Operation(summary = "Buscar actores", description = "Busca actores por nombre")
    public ResponseEntity<List<Actor>> searchActors(@Parameter(description = "Nombre del actor") @RequestParam String name) {
        return ResponseEntity.ok(actorRepository.findByNameContainingIgnoreCase(name));
    }
}
