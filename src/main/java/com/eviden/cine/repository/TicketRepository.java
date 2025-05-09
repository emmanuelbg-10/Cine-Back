package com.eviden.cine.repository;

import com.eviden.cine.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository <Ticket, Long> {
    Optional<Ticket> findByName(String name);
}
