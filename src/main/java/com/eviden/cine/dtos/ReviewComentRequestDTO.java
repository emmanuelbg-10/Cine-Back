package com.eviden.cine.dtos;


public record ReviewComentRequestDTO(
        Long reviewId, //este dto lo vamos a utilizar para crear y editar pero la id solo es para creaar
        String text
) {}
