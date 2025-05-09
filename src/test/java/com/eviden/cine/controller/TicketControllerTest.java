package com.eviden.cine.controller;

import com.eviden.cine.model.Ticket;
import com.eviden.cine.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TicketControllerTest {

    private TicketController controller;
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketService = mock(TicketService.class);
        controller = new TicketController(ticketService);
    }

    @Test
    void testGetAllTickets() {
        List<Ticket> tickets = List.of(
                new Ticket(), new Ticket()
        );

        when(ticketService.getAllTickets()).thenReturn(tickets);

        ResponseEntity<List<Ticket>> response = controller.getAllTickets();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tickets, response.getBody());
        verify(ticketService).getAllTickets();
    }
}
