package com.eviden.cine.controller;

import com.eviden.cine.dtos.MessageRequestDTO;
import com.eviden.cine.dtos.MessageResponseDTO;
import com.eviden.cine.model.Message;
import com.eviden.cine.model.User;
import com.eviden.cine.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageControllerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageController messageController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateMessage() {
        MessageRequestDTO request = MessageRequestDTO.builder()
                .foroId(1L)
                .authorId(1L)
                .text("Nuevo mensaje")
                .build();

        Message message = Message.builder()
                .id(1L)
                .author(new User())
                .text("Nuevo mensaje")
                .createdAt(LocalDateTime.now())
                .build();

        when(messageService.createMessage(request)).thenReturn(message);

        ResponseEntity<MessageResponseDTO> response = messageController.createMessage(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Nuevo mensaje", response.getBody().getText());
        verify(messageService, times(1)).createMessage(request);
    }

    @Test
    void testGetMessagesByForo() {
        MessageResponseDTO messageResponse = MessageResponseDTO.builder()
                .id(1L)
                .text("Mensaje raíz")
                .authorEmail("usuario")
                .createdAt(LocalDateTime.now())
                .replies(List.of())
                .build();

        when(messageService.getMessagesByForo(1L)).thenReturn(List.of(messageResponse));

        ResponseEntity<List<MessageResponseDTO>> response = messageController.getMessagesByForo(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("Mensaje raíz", response.getBody().get(0).getText());
        verify(messageService, times(1)).getMessagesByForo(1L);
    }

    @Test
    void testDeleteMessage() {
        Long messageId = 1L;

        ResponseEntity<Void> response = messageController.deleteMessage(messageId);

        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify(messageService, times(1)).deleteMessage(messageId);
    }
}