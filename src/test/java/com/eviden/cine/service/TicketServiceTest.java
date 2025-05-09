package com.eviden.cine.service;

import com.eviden.cine.model.Ticket;
import com.eviden.cine.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketService ticketService;

    private Ticket ticket;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ticket = new Ticket();
        ticket.setId(1L);
        ticket.setName("Avengers");
        ticket.setTicketPrice(100.0);
    }

    @Test
    void testCreateTicket() {
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        Ticket saved = ticketService.createTicket(ticket);
        assertEquals(ticket, saved);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void testGetAllTickets() {
        List<Ticket> tickets = List.of(ticket);
        when(ticketRepository.findAll()).thenReturn(tickets);
        List<Ticket> result = ticketService.getAllTickets();
        assertEquals(1, result.size());
        verify(ticketRepository).findAll();
    }

    @Test
    void testGetTicketByIdFound() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        Optional<Ticket> result = ticketService.getTicketById(1L);
        assertTrue(result.isPresent());
        assertEquals(ticket, result.get());
    }

    @Test
    void testGetTicketByIdNotFound() {
        when(ticketRepository.findById(2L)).thenReturn(Optional.empty());
        Optional<Ticket> result = ticketService.getTicketById(2L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetTicketByName() {
        when(ticketRepository.findByName("Avengers")).thenReturn(Optional.of(ticket));
        Optional<Ticket> result = ticketService.getTicketByName("Avengers");
        assertTrue(result.isPresent());
        assertEquals(ticket, result.get());
    }

    @Test
    void testUpdateTicketFound() {
        Ticket updated = new Ticket();
        updated.setName("Batman");
        updated.setTicketPrice(150.0);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket result = ticketService.updateTicket(1L, updated);

        assertEquals("Batman", result.getName());
        assertEquals(150L, result.getTicketPrice());
    }

    @Test
    void testUpdateTicketNotFound() {
        when(ticketRepository.findById(2L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ticketService.updateTicket(2L, ticket));
        assertEquals("Ticket no encontrado", ex.getMessage());
    }

    @Test
    void testUpdateTicketPriceFound() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket result = ticketService.updateTicketPrice(1L, 200.0);
        assertEquals(200L, result.getTicketPrice());
    }

    @Test
    void testUpdateTicketPriceNotFound() {
        when(ticketRepository.findById(3L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ticketService.updateTicketPrice(3L, 200.0));
        assertEquals("Ticket no encontrado", ex.getMessage());
    }

    @Test
    void testDeleteTicket() {
        doNothing().when(ticketRepository).deleteById(1L);
        ticketService.deleteTicket(1L);
        verify(ticketRepository).deleteById(1L);
    }

    @Test
    void testGetTicketPriceByIdFound() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        Double price = ticketService.getTicketPriceById(1L);
        assertEquals(100.0, price);
    }

    @Test
    void testGetTicketPriceByIdNotFound() {
        when(ticketRepository.findById(4L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ticketService.getTicketPriceById(4L));
        assertEquals("Ticket no encontrado", ex.getMessage());
    }

    @Test
    void testIsTicketValidTrue() {
        when(ticketRepository.existsById(1L)).thenReturn(true);
        assertTrue(ticketService.isTicketValid(1L));
    }

    @Test
    void testIsTicketValidFalse() {
        when(ticketRepository.existsById(5L)).thenReturn(false);
        assertFalse(ticketService.isTicketValid(5L));
    }
}
