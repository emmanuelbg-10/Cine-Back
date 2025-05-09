package com.eviden.cine.dtos;

public record ReviewRequestDTO(
        int movieId,
        double rate,
        String comment
) {}