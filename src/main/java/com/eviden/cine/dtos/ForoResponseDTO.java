package com.eviden.cine.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForoResponseDTO {

    private Long id;
    private String title;
    private String description;
    private String movieTitle;
    private String ownerEmail;
    private LocalDateTime createdAt;

}
