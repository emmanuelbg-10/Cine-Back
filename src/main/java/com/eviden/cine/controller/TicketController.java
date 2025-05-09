package com.eviden.cine.controller;

import com.eviden.cine.model.Ticket;
import com.eviden.cine.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Tickets", description = "Operaciones relacionadas con los tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Operation(
            summary = "Obtener todos los tickets",
            description = "Devuelve un listado completo de tickets disponibles"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listado de tickets",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))
    )
    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets(){
        List<Ticket> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }
}
