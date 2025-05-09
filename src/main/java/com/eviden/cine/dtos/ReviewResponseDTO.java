package com.eviden.cine.dtos;

import java.time.LocalDate;

public record ReviewResponseDTO(
        int id,
        double rate,
        String comment,
        LocalDate reviewDate,
        String username
) {}