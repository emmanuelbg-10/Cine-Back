package com.eviden.cine.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomAdminResponseDTO {
    private Long idroom;
    private String nombreroom;
    private Integer capacidad;
    private int filas;
    private int columnas;
    private String regionName;
    private Long regionId;
    private List<EmisionSimpleDTO> emisiones;
}
