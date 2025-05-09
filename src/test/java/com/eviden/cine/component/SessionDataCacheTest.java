package com.eviden.cine.component;

import com.eviden.cine.dtos.AsientoTicketDTO;
import com.eviden.cine.dtos.ReservationRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SessionDataCacheTest {

    private SessionDataCache cache;
    private ReservationRequestDTO reservation;
    private String sessionId = "abc123";

    @BeforeEach
    void setUp() {
        cache = new SessionDataCache();
        reservation = new ReservationRequestDTO();
        reservation.setUserId(1L);
        reservation.setEmisionId(10L);
        reservation.setTotal(3500.0);
        reservation.setAsientos(List.of(new AsientoTicketDTO(1L, 1L,"A2")));
    }

    @Test
    void saveAndGetReservationData() {
        cache.saveReservationData(sessionId, reservation);
        ReservationRequestDTO result = cache.getReservationData(sessionId);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(10L, result.getEmisionId());
    }

    @Test
    void removeReservationData_removesCorrectly() {
        cache.saveReservationData(sessionId, reservation);
        cache.removeReservationData(sessionId);

        assertNull(cache.getReservationData(sessionId));
    }

    @Test
    void saveAndGetReservaId() {
        Long reservaId = 42L;
        cache.saveReservaId(sessionId, reservaId);

        Long result = cache.getReservaId(sessionId);

        assertNotNull(result);
        assertEquals(42L, result);
    }
}
