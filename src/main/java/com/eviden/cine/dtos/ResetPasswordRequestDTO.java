package com.eviden.cine.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequestDTO {

    @NotBlank
    private String token;

    @NotBlank
    private String newPassword;
}
