package com.eviden.cine.controller;

import com.eviden.cine.dtos.FavoriteDTO;
import com.eviden.cine.dtos.FavoriteTranslateDTO;
import com.eviden.cine.exception.CustomException;
import com.eviden.cine.model.Favorite;
import com.eviden.cine.service.FavoriteService;
import com.eviden.cine.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "Favorites", description = "Operaciones relacionadas con los favoritos de los usuarios")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final MovieService movieService;

    public FavoriteController(FavoriteService favoriteService, MovieService movieService) {
        this.favoriteService = favoriteService;
        this.movieService = movieService;

    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtener favoritos de un usuario", description = "Devuelve una lista de favoritos (DTO) del usuario indicado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Favoritos obtenidos correctamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = FavoriteDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public ResponseEntity<List<FavoriteTranslateDTO>> getFavoritesByUserId(
            Authentication authentication,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {

        String email = authentication != null ? authentication.getName() : null;
        String language = movieService.resolveLanguage(email, acceptLanguage);

        List<FavoriteTranslateDTO> favorites = favoriteService.getFavoriteTranslated(userId, language);
        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un favorito por ID", description = "Devuelve un favorito completo por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Favorito encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Favorite.class))),
            @ApiResponse(responseCode = "404", description = "Favorito no encontrado", content = @Content)
    })
    public ResponseEntity<Favorite> getFavoriteById(
            @Parameter(description = "ID del favorito") @PathVariable Long id) {
        Favorite favorite = favoriteService.getFavoriteById(id);
        return favorite != null
                ? ResponseEntity.ok(favorite)
                : ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Añadir un favorito", description = "Marca una película como favorita para un usuario determinado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Favorito añadido correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Favorite.class))),
            @ApiResponse(responseCode = "409", description = "El favorito ya existe para este usuario", content = @Content),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la petición", content = @Content)
    })
    public ResponseEntity<Favorite> addFavorite(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objeto Favorite con referencias al usuario y a la película",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Favorite.class),
                            examples = @ExampleObject(value = """
                            {
                              "user": { "userId": 1 },
                              "movie": { "id": 2 }
                            }
                            """)
                    )
            )
            @RequestBody Favorite favorite) {
        try {
            Favorite saved = favoriteService.addFavorite(
                    favorite.getUser().getUserId(),
                    favorite.getMovie().getId()
            );
            return ResponseEntity.ok(saved);
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping(value = "/remove", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Eliminar un favorito", description = "Elimina la relación de favorito entre un usuario y una película")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Favorito eliminado correctamente", content = @Content),
            @ApiResponse(responseCode = "400", description = "Error al eliminar el favorito", content = @Content)
    })
    public ResponseEntity<Void> removeFavorite(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objeto Favorite con las claves necesarias para identificar el favorito",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Favorite.class),
                            examples = @ExampleObject(value = """
                            {
                              "user": { "userId": 1 },
                              "movie": { "id": 2 }
                            }
                            """)
                    )
            )
            @RequestBody Favorite favorite) {
        try {
            favoriteService.removeFavorite(
                    favorite.getUser().getUserId(),
                    favorite.getMovie().getId()
            );
            return ResponseEntity.ok().build();
        } catch (CustomException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("/user/email/{email}")
    @Operation(summary = "Obtener favoritos de un usuario por email", description = "Devuelve una lista de favoritos del usuario indicado por su email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Favoritos obtenidos correctamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Favorite.class)))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public ResponseEntity<List<FavoriteDTO>> getFavoritesByEmail(
            @Parameter(description = "Email del usuario") @PathVariable String email) {
        List<FavoriteDTO> favorites = favoriteService.getFavoritesByEmail(email);
        return ResponseEntity.ok(favorites);
    }
}
