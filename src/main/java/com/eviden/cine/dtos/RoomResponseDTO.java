package com.eviden.cine.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponseDTO {
    private Long idroom;
    private String nombreroom;
    private int filas;
    private int columnas;
    private Long regionId;
}
