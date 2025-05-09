package com.eviden.cine.controller;

import com.eviden.cine.dtos.GenreTranslatedDTO;
import com.eviden.cine.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
@Tag(name = "Genres", description = "Operaciones relacionadas con géneros de películas")
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    @Operation(
            summary = "Obtener géneros según idioma",
            description = """
                Devuelve géneros traducidos automáticamente:
                - Si el usuario está autenticado, se usa su idioma preferido
                - Si no, se toma del header Accept-Language
                - Si no se especifica ninguno, se usa español (es) por defecto
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de géneros obtenida correctamente",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content)
    })
    public ResponseEntity<List<GenreTranslatedDTO>> getAllGenres(
            @RequestHeader(name = "Accept-Language", required = false) String acceptLang,
            Authentication authentication
    ) {
        String email = (authentication != null) ? authentication.getName() : null;
        return ResponseEntity.ok(genreService.getGenresByUserOrHeader(email, acceptLang));
    }
}
