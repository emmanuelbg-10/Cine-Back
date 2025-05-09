package com.eviden.cine.controller;

import com.eviden.cine.dtos.UpdateProfileDTO;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.UserRepository;
import com.eviden.cine.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Endpoints relacionados con la gestión de usuarios")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los usuarios", description = "Devuelve una lista de todos los usuarios registrados en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuarios obtenidos correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un usuario por ID", description = "Devuelve un usuario específico dado su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<User> getUserById(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Actualizar perfil del usuario autenticado",
            description = "Permite al usuario autenticado actualizar su nombre, correo, idioma, región y contraseña.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil actualizado correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos para actualizar el perfil",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateProfileDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "username": "nuevoNombre",
                                  "email": "nuevo@email.com",
                                  "password": "claveSegura123",
                                  "preferredLanguage": "es",
                                  "region": "ES"
                                }
                            """))
            )
            UpdateProfileDTO dto,
            Authentication authentication
    ) {
        try {
            String email = authentication.getName();
            User updated = userService.updateProfile(dto, email);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", e.getClass().getSimpleName(),
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/role/id/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar rol de usuario", description = "Permite a un administrador cambiar el rol de un usuario existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rol actualizado correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Error al actualizar el rol"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado: solo administradores")
    })
    public ResponseEntity<User> changeUserRoleById(
            @Parameter(description = "ID del usuario", example = "3") @PathVariable Long id,
            @Parameter(description = "ID del nuevo rol", example = "2") @PathVariable Long roleId
    ) {
        try {
            User updatedUser = userService.changeUserRoleById(id, roleId);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
