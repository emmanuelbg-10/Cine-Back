package com.eviden.cine.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequestDTO {
    private Long foroId;
    private Long authorId;
    private String text;
    private Long parentMessageId; // null si es raíz
}
