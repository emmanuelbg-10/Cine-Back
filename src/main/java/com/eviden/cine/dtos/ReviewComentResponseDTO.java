package com.eviden.cine.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record ReviewComentResponseDTO(
        Long id,
        String text,
        String authorUsername, // visible en frontend
        String authorEmail,    // solo para control
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {}
