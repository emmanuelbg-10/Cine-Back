package com.eviden.cine.repository;

import com.eviden.cine.model.ReservationDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationDetailsRepository extends JpaRepository<ReservationDetails, Long> {
}
