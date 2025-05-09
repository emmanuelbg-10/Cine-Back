package com.eviden.cine.dtos;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50)
    private String username;

    @Email(message = "Debe tener un formato de correo válido")
    @NotBlank
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    private String preferredLanguage;

    private String region;
}