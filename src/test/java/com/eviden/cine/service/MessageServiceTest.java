package com.eviden.cine.service;

import com.eviden.cine.dtos.MessageRequestDTO;
import com.eviden.cine.dtos.MessageResponseDTO;
import com.eviden.cine.model.Foro;
import com.eviden.cine.model.Message;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.ForoRepository;
import com.eviden.cine.repository.MessageRepository;
import com.eviden.cine.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ForoRepository foroRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageService messageService;

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

        Foro foro = Foro.builder().id(1L).build();
        User author = User.builder().userId(1L).email("usuario").build();
        Message message = Message.builder()
                .id(1L)
                .foro(foro)
                .author(author)
                .text("Nuevo mensaje")
                .createdAt(LocalDateTime.now())
                .build();

        when(foroRepository.findById(1L)).thenReturn(Optional.ofNullable(foro));
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(author));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        Message result = messageService.createMessage(request);

        assertNotNull(result);
        assertEquals("Nuevo mensaje", result.getText());
        verify(foroRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void testGetMessagesByForo() {
        Foro foro = Foro.builder().id(1L).build();
        User author = User.builder().userId(1L).email("usuario").build();

        Message rootMessage = Message.builder()
                .id(1L)
                .foro(foro)
                .author(author)
                .text("Mensaje raíz")
                .createdAt(LocalDateTime.now())
                .build();

        Message replyMessage = Message.builder()
                .id(2L)
                .foro(foro)
                .author(author)
                .parentMessage(rootMessage)
                .text("Respuesta")
                .createdAt(LocalDateTime.now())
                .build();

        when(messageRepository.findByForoId(1L)).thenReturn(List.of(rootMessage, replyMessage));

        List<MessageResponseDTO> result = messageService.getMessagesByForo(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Mensaje raíz", result.get(0).getText());
        assertEquals(1, result.get(0).getReplies().size());
        assertEquals("Respuesta", result.get(0).getReplies().get(0).getText());
        verify(messageRepository, times(1)).findByForoId(1L);
    }

    @Test
    void testCreateMessageWithParent() {
        MessageRequestDTO request = MessageRequestDTO.builder()
                .foroId(1L)
                .authorId(1L)
                .parentMessageId(2L)
                .text("Nuevo mensaje")
                .build();

        Foro foro = Foro.builder().id(1L).build();
        User author = User.builder().userId(1L).email("usuario").build();
        Message parentMessage = Message.builder().id(2L).foro(foro).author(author).text("Mensaje padre").build();
        Message message = Message.builder()
                .id(3L)
                .foro(foro)
                .author(author)
                .parentMessage(parentMessage)
                .text("Nuevo mensaje")
                .createdAt(LocalDateTime.now())
                .build();

        when(foroRepository.findById(1L)).thenReturn(Optional.ofNullable(foro));
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(author));
        when(messageRepository.findById(2L)).thenReturn(Optional.of(parentMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        Message result = messageService.createMessage(request);

        assertNotNull(result);
        assertEquals("Nuevo mensaje", result.getText());
        assertEquals(parentMessage, result.getParentMessage());
        verify(foroRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(messageRepository, times(1)).findById(2L);
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void deleteMessage() {
        Long messageId = 1L;
        Message message = Message.builder().id(messageId).build();

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        messageService.deleteMessage(messageId);

        verify(messageRepository, times(1)).delete(message);
    }
}