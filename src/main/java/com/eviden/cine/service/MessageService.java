package com.eviden.cine.service;

import com.eviden.cine.dtos.MessageRequestDTO;
import com.eviden.cine.dtos.MessageResponseDTO;
import com.eviden.cine.model.Foro;
import com.eviden.cine.model.Message;
import com.eviden.cine.model.User;
import com.eviden.cine.repository.ForoRepository;
import com.eviden.cine.repository.MessageRepository;
import com.eviden.cine.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ForoRepository foroRepository;
    private final UserRepository userRepository;

    public Message createMessage(MessageRequestDTO dto) {
        Foro foro = foroRepository.findById(dto.getForoId())
                .orElseThrow(() -> new IllegalArgumentException("Foro no encontrado"));
        User author = userRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Message parent = null;
        if (dto.getParentMessageId() != null) {
            parent = messageRepository.findById(dto.getParentMessageId())
                    .orElseThrow(() -> new IllegalArgumentException("Mensaje padre no encontrado"));
        }

        Message message = Message.builder()
                .foro(foro)
                .author(author)
                .parentMessage(parent)
                .text(dto.getText())
                .upvotes(0)
                .downvotes(0)
                .createdAt(LocalDateTime.now())
                .build();

        return messageRepository.save(message);
    }

    public List<MessageResponseDTO> getMessagesByForo(Long foroId) {
        List<Message> allMessages = messageRepository.findByForoId(foroId);

        // Mapa para agrupar respuestas por parentId
        Map<Long, List<Message>> repliesMap = new HashMap<>();
        List<Message> roots = new ArrayList<>();

        for (Message m : allMessages) {
            if (m.getParentMessage() == null) {
                roots.add(m);
            } else {
                Long parentId = m.getParentMessage().getId();
                repliesMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(m);
            }
        }

        // Construcción recursiva del árbol
        return roots.stream()
                .map(m -> buildResponseTree(m, repliesMap))
                .toList();
    }

    private MessageResponseDTO buildResponseTree(Message message, Map<Long, List<Message>> repliesMap) {
        List<MessageResponseDTO> replies = repliesMap.getOrDefault(message.getId(), Collections.emptyList())
                .stream()
                .map(reply -> buildResponseTree(reply, repliesMap))
                .toList();

        return MessageResponseDTO.builder()
                .id(message.getId())
                .text(message.getText())
                .authorEmail(message.getAuthor().getEmail())
                .createdAt(message.getCreatedAt())
                .upvotes(message.getUpvotes())
                .downvotes(message.getDownvotes())
                .replies(replies)
                .build();
    }

    public void deleteMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado"));
        messageRepository.delete(message);
    }
}

