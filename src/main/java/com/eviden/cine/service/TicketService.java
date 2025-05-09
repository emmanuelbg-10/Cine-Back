package com.eviden.cine.service;

import com.eviden.cine.model.Ticket;
import com.eviden.cine.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    private static final String TICKET_NOT_FOUND_MSG = "Ticket no encontrado";

    private final TicketRepository ticketRepository;

    @Autowired
    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Ticket createTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    public Optional<Ticket> getTicketByName(String name) {
        return ticketRepository.findByName(name);
    }

    public Ticket updateTicket(Long id, Ticket updatedTicket) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(TICKET_NOT_FOUND_MSG));

        ticket.setName(updatedTicket.getName());
        ticket.setTicketPrice(updatedTicket.getTicketPrice());

        return ticketRepository.save(ticket);
    }

    public Ticket updateTicketPrice(Long id, Double newPrice) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(TICKET_NOT_FOUND_MSG));

        ticket.setTicketPrice(newPrice);
        return ticketRepository.save(ticket);
    }

    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }

    public Double getTicketPriceById(Long id) {
        return ticketRepository.findById(id)
                .map(Ticket::getTicketPrice)
                .orElseThrow(() -> new RuntimeException(TICKET_NOT_FOUND_MSG));
    }

    public boolean isTicketValid(Long id) {
        return ticketRepository.existsById(id);
    }
}
