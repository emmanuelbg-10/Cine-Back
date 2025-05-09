package com.eviden.cine.controller;

import com.eviden.cine.dtos.MessageRequestDTO;
import com.eviden.cine.dtos.MessageResponseDTO;
import com.eviden.cine.model.Message;
import com.eviden.cine.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("new")
    public ResponseEntity<MessageResponseDTO> createMessage(@RequestBody MessageRequestDTO dto) {
        Message saved = messageService.createMessage(dto);
        MessageResponseDTO response = MessageResponseDTO.builder()
                .id(saved.getId())
                .authorEmail(saved.getAuthor().getEmail())
                .text(saved.getText())
                .upvotes(saved.getUpvotes())
                .downvotes(saved.getDownvotes())
                .createdAt(saved.getCreatedAt())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/foro/{foroId}")
    public ResponseEntity<List<MessageResponseDTO>> getMessagesByForo(@PathVariable Long foroId) {
        return ResponseEntity.ok(messageService.getMessagesByForo(foroId));
    }

    @DeleteMapping("/delete/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }
}
