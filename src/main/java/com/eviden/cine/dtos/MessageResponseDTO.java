package com.eviden.cine.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponseDTO {
    private Long id;
    private String authorEmail;
    private String text;
    private Integer upvotes;
    private Integer downvotes;
    private LocalDateTime createdAt;
    private List<MessageResponseDTO> replies; // para anidamiento
}
