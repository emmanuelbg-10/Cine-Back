package com.eviden.cine.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponseDTO {
    private Long id;
    private UserDTO user;
    private MovieMinimalDTO movie;
    private String addedDate; // Formato yyyy-MM-dd
}