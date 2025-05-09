package com.eviden.cine.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmisionSimpleDTO {
    private Long idEmision;
    private String idioma;
    private LocalDateTime fechaHoraInicio;
    private MovieSimpleDTO movie;
}
