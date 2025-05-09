package com.eviden.cine.repository;

import com.eviden.cine.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserEmail(String email);
}
