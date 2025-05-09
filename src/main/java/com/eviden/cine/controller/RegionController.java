package com.eviden.cine.controller;

import com.eviden.cine.dtos.EmisionFrontDTO;
import com.eviden.cine.model.Movie;
import com.eviden.cine.model.Region;
import com.eviden.cine.service.EmisionService;
import com.eviden.cine.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;
    private final EmisionService emisionService;

    @Operation(summary = "Obtener todas las regiones")
    @GetMapping
    public ResponseEntity<List<Region>> getAll() {
        return ResponseEntity.ok(regionService.listRegions());
    }
    @Operation(summary = "Películas en una región")
    @GetMapping("/{name}/movies")
    public ResponseEntity<List<Movie>> moviesByRegion(@PathVariable String name) {

        List<Movie> movies = regionService.moviesInRegion(name);
        return movies.isEmpty()
                ? ResponseEntity.noContent().build()      // 204 si la región existe pero aún no hay proyecciones
                : ResponseEntity.ok(movies);              // 200 con la lista
    }
    @Operation(summary = "Emisiones de un cine/región")
    @GetMapping("/{regionId}/emisiones")
    public ResponseEntity<List<EmisionFrontDTO>> emisionesPorRegion(
            @PathVariable Long regionId) {

        return ResponseEntity.ok(emisionService.emisionesPorRegion(regionId));
    }
}
