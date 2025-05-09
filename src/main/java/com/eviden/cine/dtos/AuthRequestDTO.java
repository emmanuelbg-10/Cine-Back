package com.eviden.cine.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequestDTO {

    @Email(message = "El correo debe tener un formato válido")
    @NotBlank(message = "El correo es obligatorio")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}