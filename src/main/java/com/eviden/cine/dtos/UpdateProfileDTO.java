package com.eviden.cine.dtos;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileDTO {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50)
    private String username;

    @Email(message = "Debe tener un formato de correo válido")
    @NotBlank
    private String email;

    @Nullable
    private String password;

    private String preferredLanguage;

    private String region;
}
