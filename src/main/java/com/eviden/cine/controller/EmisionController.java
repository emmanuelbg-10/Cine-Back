package com.eviden.cine.controller;

import com.eviden.cine.dtos.EmisionDTO;
import com.eviden.cine.dtos.EmisionFrontDTO;
import com.eviden.cine.dtos.EmisionResponseDTO;
import com.eviden.cine.dtos.EmisionSimple2DTO;
import com.eviden.cine.model.Emision;
import com.eviden.cine.service.EmisionService;
import com.eviden.cine.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/emisiones")
@Tag(name = "Emisiones", description = "Operaciones relacionadas con las emisiones de películas")
public class EmisionController {

    private final EmisionService emisionService;
    private final MovieService movieService;

    public EmisionController(EmisionService emisionService, MovieService movieService) {
        this.emisionService = emisionService;
        this.movieService = movieService;
    }

    @Operation(summary = "Listar todas las emisiones")
    @GetMapping
    public ResponseEntity<List<Emision>> listarTodas() {
        return ResponseEntity.ok(emisionService.obtenerTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una emisión por ID", description = "Devuelve una emisión con el nombre de la película traducido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Emisión obtenida correctamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EmisionSimple2DTO.class))),
            @ApiResponse(responseCode = "404", description = "Emisión no encontrada", content = @Content)
    })
    public ResponseEntity<EmisionSimple2DTO> obtenerPorId(
            Authentication authentication,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            @Parameter(description = "ID de la emisión") @PathVariable Long id) {

        Optional<Emision> emisionOptional = emisionService.obtenerPorId(id);
        if (emisionOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Emision emision = emisionOptional.get();

        String email = authentication != null ? authentication.getName() : null;
        String language = movieService.resolveLanguage(email, acceptLanguage);
        System.out.println("Idioma resuelto: " + language);
        String tituloTraducido = movieService.getTituloTraducido (emision.getMovie() , language);
        System.out.println("titulo traducido:" + tituloTraducido);
        System.out.println("el idioma que se le pasa"+ language);

        EmisionSimple2DTO dto = EmisionSimple2DTO.of(emision, tituloTraducido);

        return ResponseEntity.ok(dto);
    }


    // @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear una nueva emisión", description = """
            Permite crear una nueva emisión.\s
            Si no se proporciona 'idRoom', se asignará automáticamente la sala más adecuada.
           \s""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Emisión creada correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmisionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<EmisionResponseDTO> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos necesarios para crear la emisión. 'idRoom' es opcional.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EmisionDTO.class),
                            examples = {
                                    @ExampleObject(name = "Asignación manual de sala",
                                            value = """
                                                    {
                                                      "idPelicula": 1,
                                                      "idRoom": 5,
                                                      "fechaHoraInicio": "2025-06-15T20:30:00",
                                                      "idioma": "Español subtitulado",
                                                      "estado": "ACTIVO"
                                                    }
                                                    """),
                                    @ExampleObject(name = "Asignación automática de sala",
                                            value = """
                                                    {
                                                      "idPelicula": 1,
                                                      "fechaHoraInicio": "2025-06-15T20:30:00",
                                                      "idioma": "Español subtitulado",
                                                      "estado": "ACTIVO"
                                                    }
                                                    """)
                            }
                    )
            )
            @Valid @RequestBody EmisionDTO emisionDTO) {
        Emision nuevaEmision = emisionService.guardarDesdeDTO(emisionDTO);
        EmisionResponseDTO response = emisionService.toEmisionResponseDTO(nuevaEmision);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar una emisión existente", description = """
    Permite actualizar una emisión existente.
    
    - Si no se proporciona 'idRoom' (es null), se asignará automáticamente la mejor sala disponible para el horario solicitado.
    - Si se proporciona 'idRoom', se validará que esa sala esté disponible en el nuevo horario.
    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Emisión actualizada correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmisionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o sala no disponible", content = @Content),
            @ApiResponse(responseCode = "404", description = "Emisión no encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<EmisionResponseDTO> actualizar(
            @Parameter(description = "ID de la emisión a actualizar") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos actualizados de la emisión. 'idRoom' puede ser nulo para asignación automática.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EmisionDTO.class),
                            examples = {
                                    @ExampleObject(name = "Actualizar manualmente especificando sala",
                                            value = """
                                                {
                                                  "idPelicula": 1,
                                                  "idRoom": 5,
                                                  "fechaHoraInicio": "2025-06-15T22:30:00",
                                                  "idioma": "Español doblado",
                                                  "estado": "ACTIVO"
                                                }
                                                """),
                                    @ExampleObject(name = "Actualizar dejando selección automática de sala",
                                            value = """
                                                {
                                                  "idPelicula": 1,
                                                  "idRoom": null,
                                                  "fechaHoraInicio": "2025-06-15T22:30:00",
                                                  "idioma": "Español doblado",
                                                  "estado": "ACTIVO"
                                                }
                                                """)
                            }
                    )
            )
            @Valid @RequestBody EmisionDTO emisionDTO) {

        Emision emisionActualizada = emisionService.actualizarDesdeDTO(id, emisionDTO);
        EmisionResponseDTO response = emisionService.toEmisionResponseDTO(emisionActualizada);
        return ResponseEntity.ok(response);
    }



    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar una emisión por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Emisión eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Emisión no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la emisión a eliminar") @PathVariable Long id) {
        emisionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener emisiones por título de película")
    @ApiResponse(responseCode = "200", description = "Listado de emisiones")
    @GetMapping("/pelicula/{movieId}")
    public ResponseEntity<List<EmisionFrontDTO>> getEmisionesByMovieId(@PathVariable Long movieId) {
        List<EmisionFrontDTO> emisiones = emisionService.getEmisionesByMovieId(movieId)
                .stream()
                .map(EmisionFrontDTO::of)
                .toList();
        return ResponseEntity.ok(emisiones);
    }


    @Operation(summary = "Obtener emisiones por ID de sala")
    @ApiResponse(responseCode = "200", description = "Listado de emisiones")
    @GetMapping("/sala/{roomId}")
    public ResponseEntity<List<EmisionFrontDTO>> getEmisionesPorSala(@PathVariable Long roomId) {
        List<EmisionFrontDTO> emisiones = emisionService.obtenerEmisionesPorSala(roomId);
        return ResponseEntity.ok(emisiones);
    }

    @PreAuthorize("hasRole('ADMIN')") // Si quieres protegerlo con rol ADMIN, opcional
    @Operation(summary = "Generar emisiones automáticamente para toda la semana")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Emisiones generadas exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error al generar emisiones")
    })
    @PostMapping("/generar-automatico")
    public ResponseEntity<String> generarEmisionesAutomaticamente() {
        try {
            emisionService.generarEmisionesAutomaticas();
            return ResponseEntity.ok("Emisiones generadas correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al generar emisiones: " + e.getMessage());
        }
    }

}
