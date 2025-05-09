package com.eviden.cine.controller;

import com.eviden.cine.model.Asiento;
import com.eviden.cine.service.AsientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/asientos")
@Tag(name = "Asientos", description = "Operaciones relacionadas con los asientos de las rooms")
public class AsientoController {

    private final AsientoService asientoService;

    public AsientoController(AsientoService asientoService) {
        this.asientoService = asientoService;
    }

    @Operation(
            summary = "Obtener todos los asientos",
            description = "Devuelve un listado con todos los asientos disponibles"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listado de todos los asientos",
            content = @Content(mediaType = "application/json")
    )
    @GetMapping
    public ResponseEntity<List<Asiento>> obtenerTodos() {
        return ResponseEntity.ok(asientoService.obtenerTodosLosAsientos());
    }

    @Operation(summary = "Obtener un asiento por su ID")
    @ApiResponse(responseCode = "200", description = "Asiento encontrado", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404", description = "Asiento no encontrado", content = @Content)
    @GetMapping("/{id}")
    public ResponseEntity<Asiento> obtenerPorId(
            @Parameter(description = "ID del asiento") @PathVariable Long id) {
        Optional<Asiento> asiento = asientoService.obtenerAsientoPorId(id);
        return asiento.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Obtener los asientos de una room")
    @ApiResponse(responseCode = "200", description = "Asientos encontrados por ID de room")
    @GetMapping("/room/{idroom}")
    public ResponseEntity<List<Asiento>> obtenerPorroom(
            @Parameter(description = "ID de la room") @PathVariable Long idroom) {
        return ResponseEntity.ok(asientoService.obtenerAsientosPorroom(idroom));
    }

    @Operation(summary = "Eliminar un asiento por su ID")
    @ApiResponse(responseCode = "204", description = "Asiento eliminado correctamente")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAsiento(
            @Parameter(description = "ID del asiento") @PathVariable Long id) {
        asientoService.eliminarAsiento(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cambiar la disponibilidad de varios asientos")
    @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada para los asientos")
    @PutMapping("/disponibilidad")
    public ResponseEntity<List<Asiento>> cambiarDisponibilidadMultiple(
            @Parameter(description = "Lista de IDs de los asientos")
            @RequestBody List<Long> ids) {
        List<Asiento> actualizados = asientoService.cambiarDisponibilidad(ids);
        return ResponseEntity.ok(actualizados);
    }

    @Operation(summary = "Comprobar si el asiento esta disponible")
    @ApiResponse(responseCode = "200", description = "Asiento/s disponible/s")
    @PostMapping("/disponibilidad")
    public boolean isAsientoAvailable(
            @Parameter(description = "Lista de ID's de asientos")
            @RequestBody List<Long> ids) {
        return asientoService.isAsientoAvailable(ids);
    }
}
