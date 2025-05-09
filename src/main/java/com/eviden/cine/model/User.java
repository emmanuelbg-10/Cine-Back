package com.eviden.cine.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@Schema(description = "Entidad que representa un usuario del sistema")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del usuario", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @ToString.Include
    private Long userId;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    @Schema(description = "Nombre de usuario", example = "juanito123", minLength = 3, maxLength = 50)
    @ToString.Include
    private String username;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe tener un formato de correo válido")
    @Schema(description = "Correo electrónico del usuario", example = "juanito@mail.com", format = "email")
    @ToString.Include
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Schema(description = "Contraseña del usuario (mínimo 6 caracteres)", example = "123456", minLength = 6)
    private String password;

    @Size(max = 5, message = "El idioma debe tener máximo 5 caracteres")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "El idioma debe contener solo letras")
    @Schema(description = "Idioma preferido del usuario", example = "es")
    @ToString.Include
    private String preferredLanguage;

    @Size(max = 5, message = "La región debe tener máximo 5 caracteres")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "La región debe contener solo letras")
    @Schema(description = "Región del usuario", example = "ES")
    @ToString.Include
    private String region;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    @Schema(description = "Rol asignado al usuario")
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "user-favorites")
    @Schema(description = "Lista de favoritos del usuario", accessMode = Schema.AccessMode.READ_ONLY)
    private List<Favorite> favorites;

    public User(Role role, String username, String email, String password, String preferredLanguage, String region) {
        this.role = role;
        this.username = username;
        this.email = email;
        this.password = password;
        this.preferredLanguage = preferredLanguage;
        this.region = region;
    }
}
